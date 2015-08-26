package mjoys.agent.service.os.msg;

import mjoys.agent.service.os.Error;

public class FreePortResponse {
	public Error error = Error.Success;
	
	@Override
	public String toString() {
		return error.name();
	}
}
