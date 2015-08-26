package mjoys.agent.service.ftp;

import java.io.IOException;
import java.nio.ByteBuffer;

import mjoys.agent.Agent;
import mjoys.agent.Response;
import mjoys.agent.client.AgentAsynRpc;
import mjoys.agent.client.AgentRpcHandler;
import mjoys.agent.service.ftp.msg.EndRequest;
import mjoys.agent.service.ftp.msg.MsgType;
import mjoys.agent.service.ftp.msg.StartRequest;
import mjoys.agent.service.ftp.msg.FtpResponse;
import mjoys.agent.util.Tag;
import mjoys.frame.ByteBufferParser;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.io.SerializerException;
import mjoys.socket.tcp.server.ClientConnection;
import mjoys.socket.tcp.server.SocketServer;
import mjoys.util.Address;
import mjoys.util.ByteUnit;
import mjoys.util.Logger;
import mjoys.util.TimeUnit;

public class FtpServer {
    private boolean registered;
    private AgentAsynRpc agentAsynRpc;
    private SocketServer<FileContext> server;
    
    public final static String ServiceName = "ftp";
    public final static int BufferSize = ByteUnit.MB;
    private static final Logger logger = new Logger().addPrinter(System.out);
    
    public void start(int tcpPort, Address address) {
        this.registered = false;
        
        server = new SocketServer<FileContext>();
        if (false == server.start(tcpPort, new FileHandler())) {
        	logger.log("start socket server failed");
            return;
        }
        
        agentAsynRpc = new AgentAsynRpc();
        if (false == agentAsynRpc.start(address, new FrameHandler())) {
            server.stop();
            logger.log("start ftp failed");
            return;
        }
        
        agentAsynRpc.setTag(new Tag(Agent.PublicTag.servicename.name(), ServiceName));
    }
    
    class FrameHandler implements AgentRpcHandler<ByteBuffer> {
        @Override
        public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> idFrame) {
            if (idFrame.tag != Agent.PublicService.Agent.id) {
                TV<ByteBuffer> cmdFrame = ByteBufferParser.parseTV(idFrame.body);
                if (cmdFrame == null) {
                    logger.log("frame is null");
                    return;
                }
                try {
                	String error = processCmd(rpc, cmdFrame);
                	FtpResponse response = new FtpResponse();
                	response.error = error;
                	rpc.sendMsg(idFrame.tag, cmdFrame.tag, response);
                } catch(Exception e) {
                	logger.log("process cmd exception:", e);
                }
            } else {
                processAgentMsg(idFrame, rpc);
            }
        }
        
        private void processAgentMsg(TLV<ByteBuffer> idFrame, AgentAsynRpc rpc) {
        	TV<ByteBuffer> responseFrame = Agent.parseMsgFrame(idFrame.body);
        	Agent.MsgType msgType = Agent.getMsgType(responseFrame.tag);
            Response response = Agent.decodeAgentResponse(msgType, new ByteBufferInputStream(responseFrame.body), rpc.getSerializer());
            if (msgType == Agent.MsgType.SetTag || response != null && response.getError() == Agent.Error.Success) {
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
                StartRequest cmd = (StartRequest) rpc.getSerializer().decode(new ByteBufferInputStream(frame.body), StartRequest.class); 
                ctx = FileContext.newFileContext(cmd.getName(), error);
                if (ctx == null) {
                    return error.toString();
                }
                
                server.setClientContext(cmd.getConnectionAddress(), ctx);
            }
            else if (type == MsgType.End.ordinal()) {
                EndRequest cmd = (EndRequest) rpc.getSerializer().decode(new ByteBufferInputStream(frame.body), EndRequest.class);
                ClientConnection<FileContext> conn= server.getConnection(cmd.getAddress());
                ctx = conn.getContext();
                
                if (ctx == null) {
                    error.append("can't find connection: address=" + cmd.getAddress());
                    return error.toString();
                }
                
                try {
	                // received all file data
                	ctx.setExpectedRecvLength(cmd.getLength());
	                if (cmd.getLength() == ctx.getRecvLength()) {
	                	conn.getSocket().close();
	                } else {
	                	conn.getSocket().setSoTimeout(5 * TimeUnit.Second);
	                }
                }
                catch (IOException e) {
                	logger.log("socket io exception", e);
                }
            }
            
            return "";
        }
    }
}