package service.text;

import mjoys.util.Address;
import mjoys.util.Logger;
import mjoys.util.StringUtil;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.IdFrame;
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
    
    public class TextHandler implements AgentRpcHandler {
        @Override
        public void handle(AgentAsynRpc rpc, IdFrame frame) {
            if (frame.getId() != AgentProtocol.PublicService.Agent.id) {
                String text = StringUtil.getUTF8String(frame.getBody());
                logger.log(text);
            } else {
                logger.log("agent response: %s", rpc.getSerializer().decodeResponse(frame.getBody()).toString());
            }
        }
    }
}