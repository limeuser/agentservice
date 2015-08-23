package service.os.msg;

import service.os.Error;
import mjoys.util.Address;

public class AllocatePortResponse {
	public Error error = Error.Success; 
	public Address.Protocol protocol;
	public int port;
}
