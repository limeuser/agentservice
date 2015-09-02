package mjoys.agent.service.ftp;

import java.io.IOException;

import mjoys.socket.tcp.server.ClientConnection;
import mjoys.socket.tcp.server.ClientConnectionHandler;
import mjoys.util.Logger;

public class FileHandler implements ClientConnectionHandler<FileContext> {
	private static final Logger logger = new Logger().addPrinter(System.out);
    @Override
    public int handle(ClientConnection<FileContext> connection) {
        FileContext ctx = connection.getContext();
        if (ctx == null) {
        	return 0;
        }
        byte[] buffer = ctx.getBuffer();
        
        if (ctx.getRemainingSize() == 0) {
            if (!ctx.flush()) {
                return -1;
            }
        }
        
        try {
        	int length = connection.getSocket().getInputStream().read(buffer, ctx.getDataLength(), ctx.getRemainingSize());
        	ctx.read(length);
        	return length;
        } catch (IOException e) {
        	ctx.done();
        	logger.log("recv file exception:", e);
        	return -1;
        }
    }
}
