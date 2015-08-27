package mjoys.agent.service.os;

import mjoys.agent.client.AgentSyncRpc;
import mjoys.agent.service.os.msg.AllocatePortRequest;
import mjoys.agent.service.os.msg.AllocatePortResponse;
import mjoys.agent.service.os.msg.Error;
import mjoys.agent.service.os.msg.FreePortRequest;
import mjoys.agent.service.os.msg.FreePortResponse;
import mjoys.agent.service.os.msg.MsgType;
import mjoys.util.Address;
import mjoys.util.Logger;

public class OSClient {
	private AgentSyncRpc rpc;
	private static Logger logger = new Logger().addPrinter(System.out);
	
	public OSClient(AgentSyncRpc rpc) {
		this.rpc = rpc;
	}
	
	public int allocatePort(int serviceId, Address.Protocol protocol) {
		AllocatePortRequest request = new AllocatePortRequest();
		request.protocol = protocol;
		AllocatePortResponse response = rpc.call(serviceId, MsgType.AllocatePort.ordinal(), request, AllocatePortResponse.class);
		if (response == null || response.error != Error.Success) {
			logger.log("allocate port failed");
			return -1;
		} else {
			logger.log("call os server: allocate port success");
		}
		
		return response.port;
	}
	
	public boolean freePort(int serviceId, Address.Protocol protocol, int port) {
		FreePortRequest request = new FreePortRequest();
		request.protocol = protocol;
		request.port = port;
		FreePortResponse response = rpc.call(serviceId, MsgType.FreePort.ordinal(), request, FreePortResponse.class);
		if (response == null || response.error != Error.Success) {
			logger.log("free port failed:%d", port);
			return false;
		} else {
			logger.log("call os server: free port success");
			return true;
		}
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
