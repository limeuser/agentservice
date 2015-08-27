package mjoys.agent.service.os;

import mjoys.agent.util.AgentCfg;
import mjoys.util.Address;

public class Main {
	public static void main(String[] args) {
		OSServer server = new OSServer();
		server.start(Address.parse(AgentCfg.instance.getServerAddress()));
	}
}
