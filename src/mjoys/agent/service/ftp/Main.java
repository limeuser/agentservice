package mjoys.agent.service.ftp;

import mjoys.util.Address;
import mjoys.util.NumberUtil;

public class Main {
	public static void main(String[] args) {
		FtpServer server = new FtpServer();
		if (args.length != 3) {
			System.out.println("please set ftp server port and agent address, for example:\r\n6000 tcp://192.168.1.103:6500");
			return;
		}
		
		Integer ftpPort = NumberUtil.parseInt(args[1]);
		Address agentAddress = Address.parse(args[2]);
		if (ftpPort == null || agentAddress == null) {
			System.out.println("bad paramter, example: 6000 tcp://192.168.1.103:6500");
			return;
		}
		
		server.start(ftpPort, agentAddress);
	}
}
