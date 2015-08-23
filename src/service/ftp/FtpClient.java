package service.ftp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import mjoys.socket.tcp.client.SocketClient;
import mjoys.util.Address;
import mjoys.util.ByteUnit;
import mjoys.util.Logger;
import service.ftp.msg.EndRequest;
import service.ftp.msg.EndResponse;
import service.ftp.msg.MsgType;
import service.ftp.msg.StartRequest;
import service.ftp.msg.StartResponse;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.GetIdResponse;
import cn.oasistech.agent.client.AgentSyncRpc;
import cn.oasistech.util.Tag;

public class FtpClient {
    private int ftpAgentId;
    private SocketClient socket;
    private AgentSyncRpc agentSyncRpc;
    
    private byte[] buffer = new byte[ByteUnit.MB];
    
    private static final Logger logger = new Logger().addPrinter(System.out);
    
    public void connect(Address ftpAddress, Address agentAddress) {
        socket = new SocketClient();
        if (socket.connect(ftpAddress) == false) {
            return;
        }
        
        agentSyncRpc = new AgentSyncRpc();
        if (false == agentSyncRpc.start(agentAddress)) {
            return;
        }

        GetIdResponse response = agentSyncRpc.getId(new Tag(AgentProtocol.PublicTag.servicename.name(), "ftp"));
        if (response == null) {
            logger.log("can't find ftp service");
            return;
        }
        
        if (!response.getError().equals(AgentProtocol.Error.Success)) {
            logger.log("can't find ftp service: error=%s", response.getError());
            return;
        }
        
        ftpAgentId = response.getIds().get(0);
    }

    public void upload(String dst, String src) {
        if (ftpAgentId <= 0) {
            logger.log("disconnected");
            return;
        }
        
        File file = new File(src);
        if (!file.exists()) {
            return;
        }
        
        if (start(dst) == false) {
            return;
        }
        
        if (send(file) == false) {
            return;
        }
        
        if (done(dst) == false) {
            return;
        }
    }
    
    private boolean start(String dst) {
        StartRequest request = new StartRequest();
        request.setConnectionAddress(this.socket.getLocalAddress().toString());
        request.setName(dst);
        request.setPath(dst);
        
        StartResponse response = agentSyncRpc.call(ftpAgentId, MsgType.Start.ordinal(), request, StartResponse.class);
        return response != null && response.error == Error.Success;
    }
    
    private boolean done(String dst) {
        // done
        EndRequest request = new EndRequest();
        request.setAddress(this.socket.getLocalAddress().toString());
        
        EndResponse response = agentSyncRpc.call(ftpAgentId, MsgType.End.ordinal(), request, EndResponse.class);
        return response != null && response.error == Error.Success;
    }
    
    private boolean send(File file) {
        int length;
        BufferedInputStream reader = null;
        try {
            reader = new BufferedInputStream(new FileInputStream(file));
            while ((length = reader.read(buffer, 0, buffer.length)) > 0) {
                socket.send(buffer, 0, length);
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
