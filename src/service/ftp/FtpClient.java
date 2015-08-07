package service.ftp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mjoys.socket.tcp.client.SocketClient;
import mjoys.util.Address;
import mjoys.util.ByteUnit;
import mjoys.util.Logger;
import mjoys.util.Serializer;
import mjoys.util.StringUtil;
import mjoys.util.TLVFrame;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.GetIdRequest;
import cn.oasistech.agent.GetIdResponse;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.agent.client.AgentSyncRpc;
import cn.oasistech.util.Tag;

public class FtpClient {
    private int ftpAgentId;
    private SocketClient socket;
    private Serializer serializer;
    private AgentSyncRpc agentSyncRpc;
    
    private byte[] buffer = new byte[ByteUnit.MB];
    
    private static final Logger logger = new Logger().addPrinter(System.out);
    
    public void start(Address ftpAddress, Address agentAddress) {
        socket = new SocketClient();
        if (socket.connect(ftpAddress) == false) {
            return;
        }
        
        agentSyncRpc = new AgentSyncRpc();
        if (false == agentSyncRpc.start(agentAddress)) {
            return;
        }
        
        GetIdRequest request = new GetIdRequest();
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(AgentProtocol.PublicTag.servicename.name(), "ftp"));
        request.setTags(tags);
        GetIdResponse response = agentSyncRpc.getId(tags);
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
    

    public void transfer(String dst, String src) {
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
        StartCmd cmd = new StartCmd();
        cmd.setConnectionAddress(this.socket.getLocalAddress().toString());
        cmd.setName(dst);
        cmd.setPath(dst);
        byte[] data = serializer.encode(cmd);
        TLVFrame frame = new TLVFrame();
        frame.setType(MsgType.start.ordinal());
        frame.setLength(data.length);
        frame.setValue(data);
        agentSyncRpc.sendTo(ftpAgentId, data);
        
        IdFrame response = agentSyncRpc.recv();
        if (response == null) {
            return false;
        }
        
        List<TLVFrame> frames = TLVFrame.parseTLVFrames(response.getBody(), response.getBodyLength());
        if (frames.size() != 1) {
            return false;
        }
        
        frame = frames.get(0);
        String error = StringUtil.getUTF8String(frame.getValue());
        if (StringUtil.isNotEmpty(error)) {
            logger.log("can't send file: dst=%s, reason=%s", dst, error);
            return false;
        }
        
        return true;
    }
    
    private boolean done(String dst) {
        // done
        EndCmd cmd = new EndCmd();
        cmd.setAddress(this.socket.getLocalAddress().toString());
        
        byte[] data = serializer.encode(cmd);
        TLVFrame frame = new TLVFrame();
        frame.setType(MsgType.end.ordinal());
        frame.setLength(data.length);
        frame.setValue(data);
        agentSyncRpc.sendTo(ftpAgentId, data);
        IdFrame response = agentSyncRpc.recv();
        
        List<TLVFrame> frames = TLVFrame.parseTLVFrames(response.getBody(), response.getBodyLength());
        if (frames == null || frames.size() != 1) {
            return false;
        }
        
        frame = frames.get(0);
        String error = StringUtil.getUTF8String(frame.getValue());
        if (StringUtil.isNotEmpty(error)) {
            logger.log("can't send file: dst=%s, reason=%s", dst, error);
            return false;
        }
        
        return true;
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
