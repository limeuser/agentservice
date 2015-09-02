package mjoys.agent.service.os;

import mjoys.util.Address;
import mjoys.util.Formater;

public class Main {
	public static void main(String[] args) {
		System.out.println(String.format("args: %s", Formater.formatArray(args)));
		if (args.length != 1) {
			System.out.println("please set agent address, for example: tcp://192.168.1.103:6500");
			return;
		}
		
		Address agentAddress = Address.parse(args[0]);
		if (agentAddress == null) {
			System.out.println("bad agent address, for example: tcp://192.168.1.103:6500");
			return;
		}
		
		OSServer server = new OSServer();
		server.start(agentAddress);
	}
}