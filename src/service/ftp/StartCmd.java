package service.ftp;

public class StartCmd {
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getConnectionAddress() {
        return connectionAddress;
    }
    public void setConnectionAddress(String connectionAddress) {
        this.connectionAddress = connectionAddress;
    }
    private String path;
    private String name;
    private String connectionAddress;
}
