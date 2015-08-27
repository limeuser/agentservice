package mjoys.agent.service.os.msg;

import mjoys.util.Address;
import mjoys.util.Formater;

public class AllocatePortResponse {
	public Error error = Error.Success; 
	public Address.Protocol protocol;
	public int port;
	
	@Override
	public String toString() {
		return Formater.formatEntries("error", error.name(), "protocol", protocol.name(), "port", port);
	}
}
