package mjoys.agent.service.text;

import java.nio.ByteBuffer;

import mjoys.agent.Agent;
import mjoys.agent.client.AgentAsynRpc;
import mjoys.agent.client.AgentRpcHandler;
import mjoys.agent.util.Tag;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.util.Address;
import mjoys.util.Formater;
import mjoys.util.Logger;

public class TextServer {
    private AgentAsynRpc rpc;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public void start(Address address) {
        rpc = new AgentAsynRpc();
        if (rpc.start(address, new TextHandler()) == false) {
            return;
        }
        rpc.setTag(new Tag(Agent.PublicTag.servicename.name(), "text"));
    }
    
    public void stop() {
        rpc.stop();
    }
    
    public class TextHandler implements AgentRpcHandler<ByteBuffer> {
        @Override
        public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> frame) {
            if (frame.tag != Agent.PublicService.Agent.id) {
                logger.log("text server recv msg: %d(%d): %s", frame.tag, frame.body.remaining(), Formater.formatBytes(frame.body));
            } else {
            	TV<ByteBuffer> msg = Agent.parseMsgFrame(frame.body);
            	Agent.MsgType type = Agent.getMsgType(msg.tag);
                logger.log("text server recv agent response: %s", Agent.decodeAgentResponse(type, new ByteBufferInputStream(msg.body), rpc.getSerializer()));
            }
        }
    }
}