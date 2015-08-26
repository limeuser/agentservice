package mjoys.agent.service.os.msg;

import mjoys.util.Formater;

public class KillRequest {
	public int pid;
	
	@Override
	public String toString() {
		return Formater.formatEntry("pid", pid);
	}
}
