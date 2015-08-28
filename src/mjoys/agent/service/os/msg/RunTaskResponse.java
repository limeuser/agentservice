package mjoys.agent.service.os.msg;

import mjoys.util.Formater;

public class RunTaskResponse {
	public String error;
	public int pid;
	
	@Override
	public String toString() {
		return Formater.formatEntries("error", error, "pid", pid);
	}
}
