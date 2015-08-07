package service.ftp;

import java.io.IOException;

import mjoys.socket.tcp.server.ClientConnection;
import mjoys.socket.tcp.server.ClientConnectionHandler;

public class FileHandler implements ClientConnectionHandler<FileContext> {
    @Override
    public int handle(ClientConnection<FileContext> connection) {
        try {
            return read(connection);
        } catch (IOException e) {
            return -1;
        }
    }
    
    private int read(ClientConnection<FileContext> connection) throws IOException {
        FileContext ctx = connection.getContext();
        byte[] buffer = ctx.getBuffer();
        
        if (ctx.getRemainingSize() == 0) {
            if (!ctx.flush()) {
                return -1;
            }
        }
        
        return connection.getSocket().getInputStream().read(buffer, ctx.getDataLength(), ctx.getRemainingSize());
    }
}
