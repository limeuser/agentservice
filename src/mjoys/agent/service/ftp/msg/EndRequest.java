package mjoys.agent.service.ftp.msg;

public class EndRequest {
    private String address;
    private int length;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
}
