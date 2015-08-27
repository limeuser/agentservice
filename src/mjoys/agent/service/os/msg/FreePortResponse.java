package mjoys.agent.service.os.msg;


public class FreePortResponse {
	public Error error = Error.Success;
	
	@Override
	public String toString() {
		return error.name();
	}
}
