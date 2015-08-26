package mjoys.agent.service.text;

import java.nio.ByteBuffer;

import mjoys.agent.client.AgentSyncRpc;
import mjoys.util.Formater;
import mjoys.util.Logger;
import mjoys.util.StringUtil;

public class TextClient {
	private final static Logger logger = new Logger().addPrinter(System.out);
	
	private AgentSyncRpc rpc;
	public TextClient(AgentSyncRpc rpc) {
		this.rpc = rpc;
	}
	
	public void send(int id, String text) {
		ByteBuffer buf = ByteBuffer.wrap(StringUtil.toBytes(text, "UTF-8"));
		logger.log("to %d(%d): %s", id, buf.remaining(), Formater.formatBytes(buf));
		this.rpc.send(id, buf);
	}
}
