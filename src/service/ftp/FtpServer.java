package service.ftp;

import mjoys.socket.tcp.server.SocketServer;
import mjoys.util.Address;
import mjoys.util.ByteUnit;
import mjoys.util.Logger;
import mjoys.util.Serializer;
import mjoys.util.StringUtil;
import mjoys.util.TLVFrame;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
import cn.oasistech.agent.Response;
import cn.oasistech.agent.client.AgentAsynRpc;
import cn.oasistech.agent.client.AgentRpcHandler;
import cn.oasistech.util.Tag;

public class FtpServer {
    private boolean registered;
    private Serializer serializer;
    private AgentAsynRpc agentAsynRpc;
    private SocketServer<FileContext> server;
    
    public final static String ServiceName = "ftp";
    public final static int BufferSize = ByteUnit.MB;
    private static final Logger logger = new Logger().addPrinter(System.out);
    
    public void start(int tcpPort, Address address) {
        this.registered = false;
        
        server = new SocketServer<FileContext>();
        if (false == server.start(tcpPort, new FileHandler())) {
            return;
        }
        
        agentAsynRpc = new AgentAsynRpc();
        if (false == agentAsynRpc.start(address, new FrameHandler())) {
            server.stop();
            logger.log("start ftp failed");
            return;
        }
        
        agentAsynRpc.setTag(new Tag(AgentProtocol.PublicTag.servicename.name(), ServiceName));
    }
    
    class FrameHandler implements AgentRpcHandler {
        @Override
        public void handle(AgentAsynRpc rpc, IdFrame idFrame) {
            if (idFrame.getId() != AgentProtocol.PublicService.Agent.id) {
                TLVFrame tlv = TLVFrame.parseTLVFrame(idFrame.getBody(), idFrame.getBodyLength());
                if (tlv == null) {
                    logger.log("frame is null");
                    return;
                }
                
                String error = processCmd(rpc, tlv);
                byte[] data = StringUtil.toBytes(error, "UTF-8");
                TLVFrame responseFrame = new TLVFrame();
                responseFrame.setType(tlv.getType());
                responseFrame.setLength(data.length);
                responseFrame.setValue(data);
                rpc.sendTo(idFrame.getId(), data, 0, data.length);
            } else {
                processAgentMsg(idFrame);
            }
        }
        
        private void processAgentMsg(IdFrame idFrame) {
            Response response = agentAsynRpc.getSerializer().decodeResponse(idFrame.getBody());
            if (response.getType().equals(AgentProtocol.MsgType.SetTag)) {
                logger.log("registered");
                registered = true;
            }
        }
        
        private String processCmd(AgentAsynRpc rpc, TLVFrame frame) {
            if (!registered) {
                return "not registered";
            }
            
            int type = frame.getType();
            FileContext ctx = null;
            StringBuilder error = new StringBuilder("");
            
            if (type == MsgType.start.ordinal()) {
                StartCmd cmd = (StartCmd) serializer.decode(frame.getValue()); 
                ctx = FileContext.newFileContext(cmd.getName(), error);
                if (ctx == null) {
                    return error.toString();
                }
                
                server.setClientContext(cmd.getConnectionAddress(), ctx);
            }
            else if (type == MsgType.end.ordinal()) {
                EndCmd cmd = (EndCmd) serializer.decode(frame.getValue());
                ctx = server.getContext(cmd.getAddress());
                
                if (ctx == null) {
                    error.append("can't find connection: address=" + cmd.getAddress());
                    return error.toString();
                }
                
                ctx.done();
                server.disconnect(cmd.getAddress());
                
                if (ctx.done() == false) {
                    error.append("close file error: fname=").append(ctx.getFile().getAbsolutePath());
                    return error.toString();
                }
            }
            
            return "";
        }
    }
}