package mjoys.agent.service.ftp;

import java.io.IOException;

import mjoys.socket.tcp.server.ClientConnection;
import mjoys.socket.tcp.server.ClientConnectionHandler;

public class FileHandler implements ClientConnectionHandler<FileContext> {
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
        	return -1;
        }
    }
}
