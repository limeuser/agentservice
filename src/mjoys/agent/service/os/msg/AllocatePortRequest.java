package mjoys.agent.service.os.msg;

import mjoys.util.Address;
import mjoys.util.Formater;

public class AllocatePortRequest {
	public Address.Protocol protocol;
	
	@Override
	public String toString() {
		return Formater.formatEntry("protocol", protocol.name());
	}
}
