package mjoys.agent.service.shell;

import java.nio.ByteBuffer;

import mjoys.agent.Agent;
import mjoys.agent.client.AgentAsynRpc;
import mjoys.agent.client.AgentRpcHandler;
import mjoys.agent.util.Tag;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.util.Address;
import mjoys.util.Logger;
import mjoys.util.StringUtil;
import mjoys.util.SystemUtil;

public class ShellServer {
	private AgentAsynRpc rpc;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public static void main(String[] args) throws InterruptedException {
    	ShellServer server = new ShellServer();
    	if (server.start(Address.getTcpAddress("127.0.0.1", 1000)) == false) {
    		System.out.println("start failed");
    		return;
    	}
    	
		Thread.sleep(1000000000000L);
    }
    
    public boolean start(Address address) {
        rpc = new AgentAsynRpc();
        if (rpc.start(address, new Handler()) == false) {
            return false;
        }
        rpc.setTag(new Tag(Agent.PublicTag.servicename.name(), "shell"));
        
        return true;
    }
    
    public void stop() {
        rpc.stop();
    }
    
    public class Handler implements AgentRpcHandler<ByteBuffer> {
        @Override
        public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> frame) {
            if (frame.tag != Agent.PublicService.Agent.id) {
            	String cmd = StringUtil.getUTF8String(frame.body);
            	
            	String result = "";
            	if (StringUtil.isNotEmpty(cmd)) {
            		result = SystemUtil.run(cmd);
            	}
            	
            	rpc.send(frame.tag, ByteBuffer.wrap(result.getBytes()));
            } else {
            	TV<ByteBuffer> msg = Agent.parseMsgFrame(frame.body);
            	Agent.MsgType type = Agent.getMsgType(msg.tag);
                logger.log("text server recv agent response: %s", Agent.decodeAgentResponse(type, new ByteBufferInputStream(msg.body), rpc.getSerializer()));
            }
        }
    }
}
