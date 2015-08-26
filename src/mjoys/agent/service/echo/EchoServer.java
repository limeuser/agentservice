package mjoys.agent.service.echo;

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

public class EchoServer {
    private AgentAsynRpc agentAsynRpc;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public boolean start(Address address) {
        agentAsynRpc = new AgentAsynRpc();
        if (agentAsynRpc.start(address, new EchoHandler()) == false) {
            return false;
        }
        agentAsynRpc.setTag(new Tag(Agent.PublicTag.servicename.name(), "echo"));
        
        return true;
    }
    
    public void stop() {
        agentAsynRpc.stop();
    }
    
    public class EchoHandler implements AgentRpcHandler<ByteBuffer> {
        @Override
        public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> frame) {
            if (frame.tag != Agent.PublicService.Agent.id) {
                String text = StringUtil.getUTF8String(frame.body);
                rpc.send(frame.tag, frame.body);
                logger.log("echo server recv: %d:%s", frame.tag, text);
            } else {
            	TV<ByteBuffer> responseFrame = Agent.parseMsgFrame(frame.body);
                logger.log("echo server recv agent response: %s", Agent.decodeAgentResponse(Agent.getMsgType(responseFrame.tag), new ByteBufferInputStream(responseFrame.body), rpc.getSerializer()));
            }
        }
    }
}
