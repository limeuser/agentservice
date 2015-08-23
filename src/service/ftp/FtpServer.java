package service.ftp;

import java.nio.ByteBuffer;

import mjoys.frame.ByteBufferParser;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.io.Serializer;
import mjoys.io.SerializerException;
import mjoys.socket.tcp.server.SocketServer;
import mjoys.util.Address;
import mjoys.util.ByteUnit;
import mjoys.util.Logger;
import service.ftp.msg.EndRequest;
import service.ftp.msg.MsgType;
import service.ftp.msg.StartRequest;
import cn.oasistech.agent.AgentProtocol;
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
    
    class FrameHandler implements AgentRpcHandler<ByteBuffer> {
        @Override
        public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> idFrame) {
            if (idFrame.tag != AgentProtocol.PublicService.Agent.id) {
                TV<ByteBuffer> cmdFrame = ByteBufferParser.parseTV(idFrame.body);
                if (cmdFrame == null) {
                    logger.log("frame is null");
                    return;
                }
                try {
                	String error = processCmd(rpc, cmdFrame);
                	rpc.sendMsg(idFrame.tag, cmdFrame.tag, error);
                } catch(Exception e) {
                	logger.log("process cmd exception:", e);
                }
            } else {
                processAgentMsg(idFrame);
            }
        }
        
        private void processAgentMsg(TLV<ByteBuffer> idFrame) {
        	TV<ByteBuffer> responseFrame = AgentProtocol.parseMsgFrame(idFrame.body);
        	AgentProtocol.MsgType msgType = AgentProtocol.getMsgType(responseFrame.tag);
            Response response = AgentProtocol.decodeAgentResponse(msgType, new ByteBufferInputStream(responseFrame.body), serializer);
            if (msgType == AgentProtocol.MsgType.SetTag || response != null && response.getError() == AgentProtocol.Error.Success) {
                logger.log("registered");
                registered = true;
            }
        }
        
        private String processCmd(AgentAsynRpc rpc, TV<ByteBuffer> frame) throws SerializerException {
            if (!registered) {
                return "not registered";
            }
            
            int type = frame.tag;
            FileContext ctx = null;
            StringBuilder error = new StringBuilder("");
            
            if (type == MsgType.Start.ordinal()) {
                StartRequest cmd = (StartRequest) serializer.decode(new ByteBufferInputStream(frame.body), StartRequest.class); 
                ctx = FileContext.newFileContext(cmd.getName(), error);
                if (ctx == null) {
                    return error.toString();
                }
                
                server.setClientContext(cmd.getConnectionAddress(), ctx);
            }
            else if (type == MsgType.End.ordinal()) {
                EndRequest cmd = (EndRequest) serializer.decode(new ByteBufferInputStream(frame.body), EndRequest.class);
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