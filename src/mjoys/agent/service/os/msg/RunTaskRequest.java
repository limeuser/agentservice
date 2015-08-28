package mjoys.agent.service.os.msg;

import mjoys.util.Formater;

public class RunTaskRequest {
	public int taskId;
	public String jobName;
	public String taskName;
	
	@Override
	public String toString() {
		return Formater.formatEntries("id", taskId, "job", jobName, "task", taskName);
	}
}
