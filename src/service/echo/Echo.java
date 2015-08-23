package service.echo;

import java.nio.ByteBuffer;

import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.util.Address;
import mjoys.util.Logger;
import mjoys.util.StringUtil;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.client.AgentAsynRpc;
import cn.oasistech.agent.client.AgentRpcHandler;
import cn.oasistech.util.Tag;

public class Echo {
    private AgentAsynRpc agentAsynRpc;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public boolean start(Address address) {
        agentAsynRpc = new AgentAsynRpc();
        if (agentAsynRpc.start(address, new EchoHandler()) == false) {
            return false;
        }
        agentAsynRpc.setTag(new Tag(AgentProtocol.PublicTag.servicename.name(), "echo"));
        
        return true;
    }
    
    public void stop() {
        agentAsynRpc.stop();
    }
    
    public class EchoHandler implements AgentRpcHandler<ByteBuffer> {
        @Override
        public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> frame) {
            if (frame.tag != AgentProtocol.PublicService.Agent.id) {
                String text = StringUtil.getUTF8String(frame.body);
                rpc.send(frame.tag, frame.body);
                logger.log(text);
            } else {
            	TV<ByteBuffer> responseFrame = AgentProtocol.parseMsgFrame(frame.body);
                logger.log("agent response: %s", AgentProtocol.decodeAgentResponse(AgentProtocol.getMsgType(responseFrame.tag), new ByteBufferInputStream(responseFrame.body), rpc.getSerializer()));
            }
        }
    }
}
