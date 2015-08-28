package mjoys.agent.service.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import mjoys.agent.Agent;
import mjoys.agent.GetIdResponse;
import mjoys.agent.client.AgentSyncRpc;
import mjoys.agent.server.AgentNettyServer;
import mjoys.agent.server.AgentServer;
import mjoys.agent.service.echo.EchoClient;
import mjoys.agent.service.echo.EchoServer;
import mjoys.agent.service.ftp.FtpClient;
import mjoys.agent.service.ftp.FtpServer;
import mjoys.agent.service.os.OSClient;
import mjoys.agent.service.os.OSServer;
import mjoys.agent.service.text.TextClient;
import mjoys.agent.service.text.TextServer;
import mjoys.agent.util.AgentCfg;
import mjoys.agent.util.Tag;
import mjoys.util.Address;
import mjoys.util.Logger;
import mjoys.util.PathUtil;
import mjoys.util.SystemUtil;
import mjoys.util.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class Tester {
	private static AgentServer agentServer = new AgentNettyServer();
    private static AgentSyncRpc syncAgentRpc = new AgentSyncRpc();
    private final static Address AgentAddress = Address.parse("tcp://127.0.0.1:6500");
    
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    static {
    	agentServer.start(AgentAddress);
    	syncAgentRpc.start(AgentAddress);
    }
    
    //@Test
    public void testOs() throws InterruptedException {
    	OSServer server = new OSServer();
    	server.start(AgentAddress);
    	Thread.sleep(500);
    	
    	OSClient client = new OSClient(syncAgentRpc);
    	
        GetIdResponse response = syncAgentRpc.getId(new Tag(Agent.PublicTag.servicename.name(), "os"));
        Assert.assertNotNull(response);
        
        for (int id : response.getIds()) {
            int port = client.allocatePort(id, Address.Protocol.Tcp);
            Assert.assertTrue(port > 0);
            
            boolean success = client.freePort(id, Address.Protocol.Tcp, port);
            Assert.assertTrue(success);
        }
        
        server.stop();
    }
    
    //@Test
    public void testText() throws InterruptedException {
    	TextServer server = new TextServer();
    	server.start(AgentAddress);
    	Thread.sleep(500);
    	
    	TextClient client = new TextClient(syncAgentRpc);
    	
        GetIdResponse response = syncAgentRpc.getId(new Tag(Agent.PublicTag.servicename.name(), "text"));
        Assert.assertNotNull(response);
        for (int id : response.getIds()) {
            client.send(id, "hello");
        }
    }
    
    //@Test
    public void testEcho() throws InterruptedException {
    	EchoServer server = new EchoServer();
    	server.start(AgentAddress);
    	Thread.sleep(500);
    	
    	EchoClient client = new EchoClient(syncAgentRpc);
        GetIdResponse response = syncAgentRpc.getId(new Tag(Agent.PublicTag.servicename.name(), "echo"));
        Assert.assertNotNull(response);
        for (int id : response.getIds()) {
            String echo = client.echo(id, "hello");
            Assert.assertEquals(echo, "hello");
        }
    }
    
    @Test
    public void testFtp() throws InterruptedException, IOException {
    	int ftpPort = 6000;
    	FtpServer server = new FtpServer();
    	server.start(ftpPort, AgentAddress);
    	Thread.sleep(500);
    	
    	FtpClient client = new FtpClient(Address.parse("tcp://127.0.0.1:" + ftpPort), AgentAddress);

    	String dstPath = PathUtil.combine(AgentCfg.instance.getRoot(), "ftp", "dst");
    	String srcPath = PathUtil.combine(AgentCfg.instance.getRoot(), "ftp", "src");
    	
    	// create file
    	File dstDir = new File(dstPath);
    	if (!dstDir.isDirectory() || !dstDir.exists())
    		dstDir.mkdir();
    	
    	File srcDir = new File(srcPath);
    	if (!srcDir.isDirectory() || !srcDir.exists()) {
    		srcDir.mkdir();
    	}
    	
    	String fileName = "1.txt";
    	File srcFile = new File(srcDir, fileName);
    	if (srcFile.exists()) {
    		srcFile.delete();
    	}
    	srcFile.createNewFile();
    	
    	FileOutputStream out = new FileOutputStream(srcFile);
    	OutputStreamWriter writer = new OutputStreamWriter(out);
    	for (int i = 0 ; i < 1000; i++) {
    		writer.write("hello world\r\n");
    	}
    	out.close();
    	
    	File dstFile = new File(dstDir, fileName);
    	if (dstFile.exists()) {
    		dstFile.delete();
    	}
    	
    	client.upload(dstFile.getAbsolutePath(), srcFile.getAbsolutePath());
    	
    	dstFile = new File(dstDir, fileName);
    	
    	Thread.sleep(2 * TimeUnit.Second);
    	
    	Assert.assertTrue(dstFile.exists());
    	Assert.assertTrue(srcFile.length() == dstFile.length());
    }
}
