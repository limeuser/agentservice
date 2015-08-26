package mjoys.agent.service.echo;

import java.nio.ByteBuffer;

import mjoys.agent.client.AgentSyncRpc;
import mjoys.util.Logger;
import mjoys.util.StringUtil;

public class EchoClient {
	private AgentSyncRpc rpc;
	private final static Logger logger = new Logger().addPrinter(System.out);
	
	public EchoClient(AgentSyncRpc rpc) {
		this.rpc = rpc;
	}
	
	public String echo(int id, String text) { 
		ByteBuffer buf = this.rpc.call(id, ByteBuffer.wrap(StringUtil.toBytes(text, "UTF-8")));
		logger.log("echo client get msg: %d:%s", id, StringUtil.getUTF8String(buf));
		return StringUtil.getUTF8String(buf);
	}
}
