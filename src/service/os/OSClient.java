package service.os;

import mjoys.util.Address;
import mjoys.util.Logger;
import service.os.msg.AllocatePortRequest;
import service.os.msg.AllocatePortResponse;
import service.os.msg.FreePortRequest;
import service.os.msg.MsgType;
import cn.oasistech.agent.client.AgentSyncRpc;

public class OSClient {
	private AgentSyncRpc rpc;
	private static Logger logger = new Logger().addPrinter(System.out);
	
	public void start(AgentSyncRpc rpc) {
		this.rpc = rpc;
	}
	
	public int allocatePort(int serviceId, Address.Protocol protocol) {
		AllocatePortRequest request = new AllocatePortRequest();
		request.protocol = protocol;
		AllocatePortResponse response = rpc.call(serviceId, MsgType.AllocatePort.ordinal(), request, AllocatePortResponse.class);
		if (response == null || response.error != Error.Success) {
			logger.log("allocate port failed");
			return -1;
		}
		
		return response.port;
	}
	
	public void freePort(int serviceId, Address.Protocol protocol, int port) {
		FreePortRequest request = new FreePortRequest();
		request.protocol = protocol;
		request.port = port;
		rpc.call(serviceId, MsgType.FreePort.ordinal(), request, null);
	}
	
	public void kill(int serviceId, int pid) {
		
	}
	
	public String run(int serviceId, String cmd) {
		return "";
	}
	
	public String runTask(int serviceId, String jobName, String taskName) {
		return "";
	}
}
