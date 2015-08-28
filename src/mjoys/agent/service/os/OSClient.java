package mjoys.agent.service.os;

import mjoys.agent.client.AgentSyncRpc;
import mjoys.agent.service.os.msg.*;
import mjoys.util.Address;
import mjoys.util.Logger;
import mjoys.util.StringUtil;

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
		if (response == null || response.error != mjoys.agent.service.os.msg.Error.Success) {
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
		if (response == null || response.error != mjoys.agent.service.os.msg.Error.Success) {
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
	
	public int runTask(int serviceId, String jobName, String taskName, int taskId) {
		RunTaskRequest request = new RunTaskRequest();
		request.jobName = jobName;
		request.taskName = taskName;
		request.taskId = taskId;
		
		RunTaskResponse response = this.rpc.call(serviceId, MsgType.RunTask.ordinal(), request, RunTaskResponse.class);
		if (response == null || !response.error.isEmpty()) {
			logger.log("run task failed: %s, error:%s", request.toString());
			return -1;
		} else {
			logger.log("run task success: %s", request.toString());
			return response.pid;
		}
	}
}
