package mjoys.agent.service.os;

import mjoys.util.Address;

public class Main {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("please set agent address, for example: tcp://192.168.1.103:6500");
			return;
		}
		
		Address agentAddress = Address.parse(args[1]);
		if (agentAddress == null) {
			System.out.println("bad agent address, for example: tcp://192.168.1.103:6500");
			return;
		}
		
		OSServer server = new OSServer();
		server.start(agentAddress);
	}
}