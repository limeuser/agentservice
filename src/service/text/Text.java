package service.text;

import java.nio.ByteBuffer;
import mjoys.util.Address;
import mjoys.util.Logger;
import mjoys.util.StringUtil;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.client.AgentAsynRpc;
import cn.oasistech.agent.client.AgentRpcHandler;
import cn.oasistech.util.Tag;

public class Text {
    private AgentAsynRpc agentAsynRpc;
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public boolean start(Address address) {
        agentAsynRpc = new AgentAsynRpc();
        
        if (agentAsynRpc.start(address, new TextHandler()) == false) {
            return false;
        }
        
        agentAsynRpc.setTag(new Tag(AgentProtocol.PublicTag.servicename.name(), "text"));
        
        return true;
    }
    
    public void stop() {
        agentAsynRpc.stop();
    }
    
    public class TextHandler implements AgentRpcHandler<ByteBuffer> {
        @Override
        public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> frame) {
            if (frame.tag != AgentProtocol.PublicService.Agent.id) {
                String text = StringUtil.getUTF8String(frame.body);
                logger.log(text);
            } else {
            	TV<ByteBuffer> msg = AgentProtocol.parseMsgFrame(frame.body);
            	AgentProtocol.MsgType type = AgentProtocol.getMsgType(msg.tag);
                logger.log("agent response: %s", AgentProtocol.decodeAgentResponse(type, new ByteBufferInputStream(msg.body), rpc.getSerializer()));
            }
        }
    }
}