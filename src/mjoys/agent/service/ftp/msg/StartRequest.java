package mjoys.agent.service.ftp.msg;

import mjoys.util.Formater;

public class StartRequest {
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getConnectionAddress() {
        return connectionAddress;
    }
    public void setConnectionAddress(String connectionAddress) {
        this.connectionAddress = connectionAddress;
    }
    private String path;
    private String connectionAddress;
    
    @Override
    public String toString() {
    	return Formater.formatEntries("path", path, "address", connectionAddress);
    }
}
