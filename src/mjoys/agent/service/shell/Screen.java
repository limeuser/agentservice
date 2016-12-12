package mjoys.agent.service.shell;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import mjoys.agent.Agent;
import mjoys.agent.GetIdResponse;
import mjoys.agent.client.AgentSyncRpc;
import mjoys.agent.util.Tag;
import mjoys.util.Address;

public class Screen {
	public static class Host {
		public AgentSyncRpc rpc;
		public String ip;
		
		public Host(String ip, AgentSyncRpc rpc) {
			this.rpc = rpc;
			this.ip = ip;
		}
	}
	
	private List<Host> hosts = new ArrayList<Host>();
	
    public static void main(String[] args) throws IOException {
    	Screen server = new Screen();
    	
    	server.repl();
    }
    
    /*
     * :+ x.x.x.x
     * :- x.x.x.x
     * :ls
     * :exit
     * */
    public void repl() throws IOException {
    	Scanner scanner = new Scanner(System.in);
    	
    	while (true) {
    		if (scanner.hasNextLine()) {
    			String line = scanner.nextLine().trim();
    			
    			// meta command
    			if (line.startsWith(":")) {
    				if (line.equals(":+")) {
    					String[] words = line.split("\\s+");
    					if (words.length < 2) {
    						println("please set ip");
    						continue;
    					}
    					
    					String ip = words[1];
    					AgentSyncRpc rpc = new AgentSyncRpc();
    					if (rpc.start(Address.getTcpAddress(ip, 4000))) {
    						hosts.add(new Host(ip, rpc));
    					} else {
    						println("connect to " + ip + " failed!");
    					}
    				} else if (line.equals(":-")) {
    					String[] words = line.split("\\s+");
    					if (words.length < 2) {
    						println("please set ip");
    						continue;
    					}
    					
    					String ip = words[1];
    					for (Host h : hosts) {
    						if (h.ip.equals(ip)) {
    							hosts.remove(h);
    							break;
    						}
    					}
    				} else if (line.equals(":ls")) {
    					for (Host h : hosts) {
    						println(h.ip);
    					}
    				} else if (line.equals(":exit")) {
    					scanner.close();
    					return;
    				} else {
    					println("bad command");
    				}
    			} else {
    				for (Host h : hosts) {
    					GetIdResponse r = h.rpc.getId(new Tag(Agent.PublicTag.servicename.name(), "shell"));
    					if (r.getError() == Agent.Error.Success) {
    						println(String.format("can't find shell service for %s: %s", h.ip, r.getError().name()));
    						continue;
    					}
    					
    					ByteBuffer result = h.rpc.call(r.getIds().get(0), ByteBuffer.wrap(line.getBytes()));
    					if (result.hasRemaining()) {
    						println(h.ip + ": " + new String(result.array()));
    					}
    				}
    			}
    		}
    	}
    }
    
    private void println(String str) {
    	System.out.println(str);
    }
}
