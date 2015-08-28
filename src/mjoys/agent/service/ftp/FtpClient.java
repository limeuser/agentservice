package mjoys.agent.service.ftp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import mjoys.agent.Agent;
import mjoys.agent.GetIdResponse;
import mjoys.agent.client.AgentSyncRpc;
import mjoys.agent.service.ftp.msg.EndRequest;
import mjoys.agent.service.ftp.msg.FtpResponse;
import mjoys.agent.service.ftp.msg.MsgType;
import mjoys.agent.service.ftp.msg.StartRequest;
import mjoys.agent.util.Tag;
import mjoys.socket.tcp.client.SocketClient;
import mjoys.util.Address;
import mjoys.util.ByteUnit;
import mjoys.util.Logger;

public class FtpClient {
    private Address ftpServer;
    private SocketClient socket;
    private AgentSyncRpc agentSyncRpc;
    private byte[] buffer = new byte[ByteUnit.MB];
    private int length = 0;
    private static final Logger logger = new Logger().addPrinter(System.out);
    
    public FtpClient(Address ftpAddress, AgentSyncRpc agentRpc) {
    	this.ftpServer = ftpAddress;
    	this.agentSyncRpc = agentRpc;
        this.socket = new SocketClient();
    }
    
    public FtpClient(Address ftpAddress, Address agentAddress) {
    	this(ftpAddress, new AgentSyncRpc());
    	this.agentSyncRpc.start(agentAddress);
    }
    
    private int getFtpServerAgentId() {
        GetIdResponse response = agentSyncRpc.getId(new Tag(Agent.PublicTag.servicename.name(), "ftp"));
        if (response == null) {
            logger.log("can't find ftp service");
            return Agent.InvalidId;
        }
        
        if (!response.getError().equals(Agent.Error.Success) || response.getIds().size() != 1) {
            logger.log("can't find ftp service: error=%s", response.getError());
            return Agent.InvalidId;
        }
        
        return response.getIds().get(0);
    }
    
    public boolean upload(String dst, String src) {
    	if (this.socket.connect(ftpServer) == false) {
    		return false;
    	}
        
        File file = new File(src);
        if (!file.exists()) {
            return false;
        }
        
        if (start(dst) == false) {
            return false;
        }
        
        if (send(file) == false) {
            return false;
        }
        
        if (done(dst) == false) {
            return false;
        }
        
        return true;
    }
    
    private boolean start(String dst) {
        StartRequest request = new StartRequest();
        request.setConnectionAddress(this.socket.getLocalAddress().toString());
        request.setPath(dst);
        
        int ftpAgentId = getFtpServerAgentId();
        if (ftpAgentId == Agent.InvalidId) {
        	logger.log("can't find ftp service on agent");
        	return false;
        }
        
        FtpResponse response = agentSyncRpc.call(ftpAgentId, MsgType.Start.ordinal(), request, FtpResponse.class);
        logger.log("ftp start response: %s", response.error);
        return response != null && response.error.isEmpty();
    }
    
    private boolean done(String dst) {
        // done
        EndRequest request = new EndRequest();
        request.setLength(this.length);
        request.setAddress(this.socket.getLocalAddress().toString());
        
        int ftpAgentId = getFtpServerAgentId();
        if (ftpAgentId == Agent.InvalidId) {
        	logger.log("can't find ftp service on agent");
        	return false;
        }
        
        FtpResponse response = agentSyncRpc.call(ftpAgentId, MsgType.End.ordinal(), request, FtpResponse.class);
        logger.log("ftp done response: %s", response.error);
        return response != null && response.error.isEmpty();
    }
    
    private boolean send(File file) {
        int length;
        this.length = 0;
        BufferedInputStream reader = null;
        try {
            reader = new BufferedInputStream(new FileInputStream(file));
            while ((length = reader.read(buffer, 0, buffer.length)) > 0) {
                socket.send(buffer, 0, length);
                this.length += length;
            }
            return true;
        } catch (IOException e) {
            logger.log("io exception:", e);
            return false;
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } 
            catch (IOException e1) {}
        }
    }
}
