package mjoys.agent.service.os;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import mjoys.agent.Agent;
import mjoys.agent.Response;
import mjoys.agent.client.AgentAsynRpc;
import mjoys.agent.client.AgentRpcHandler;
import mjoys.agent.service.os.msg.*;
import mjoys.agent.service.os.msg.Error;
import mjoys.agent.util.Tag;
import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.io.SerializerException;
import mjoys.util.Address;
import mjoys.util.Logger;
import mjoys.util.StringUtil;

public class OSServer {
	private AgentAsynRpc rpc;
	private static List<Integer> idleTcpPort = new LinkedList<Integer>();
	private final static Logger logger = new Logger().addPrinter(System.out);
	
	public void start(Address serverAddress) {
		rpc = new AgentAsynRpc();
		if (rpc.start(serverAddress, new Handler(rpc)) == false) {
			return;
		}
		
		for (int i = 1000; i < 2000; i++) 
			idleTcpPort.add(1);
		rpc.setTag(new Tag(Agent.PublicTag.servicename.name(), "os"));
	}
	
	public void stop() {
		rpc.stop();
	}
	
	public class Handler implements AgentRpcHandler<ByteBuffer> {
		private AgentAsynRpc rpc;
		public Handler(AgentAsynRpc rpc) {
			this.rpc = rpc;
		}
		
		@Override
		public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> frame) {
			int requester = frame.tag;
			if (requester == Agent.PublicService.Agent.id) {
				processAgentMsg(frame);
			} else {
				TV<ByteBuffer> request = Agent.parseMsgFrame(frame.body);
				MsgType msgType = MsgType.values()[request.tag];
				try {
					processRequest(requester, msgType, request);
				} catch (Exception e) {
					logger.log("decode request exception", e);
				}
			}
		}
		
		private void processAgentMsg(TLV<ByteBuffer> frame) {
			TV<ByteBuffer> msg = Agent.parseMsgFrame(frame.body);
			Agent.MsgType type = Agent.getMsgType(msg.tag);
			Response response = Agent.decodeAgentResponse(type, new ByteBufferInputStream(msg.body), rpc.getSerializer());
            logger.log("os server recv agent response: %s:%s", type.name(), StringUtil.toString(response));
		}
		
		private void processRequest(int requester, MsgType msgType, TV<ByteBuffer> request) throws SerializerException, IOException {
			switch (msgType) {
			case AllocatePort:
				rpc.sendMsg(requester, msgType.ordinal(), allocatePort(rpc.getSerializer().decode(new ByteBufferInputStream(request.body), AllocatePortRequest.class)));
				break;
			case FreePort:
				rpc.sendMsg(requester, msgType.ordinal(), freePort(rpc.getSerializer().decode(new ByteBufferInputStream(request.body), FreePortRequest.class)));
				break;
			case Kill:
				break;
			case Run:
				break;
			default:
				break;
			}
		}
		
		private AllocatePortResponse allocatePort(AllocatePortRequest request) {
			AllocatePortResponse response = new AllocatePortResponse();
			if (idleTcpPort.isEmpty()) {
				response.error = Error.NoIdlePort;
				logger.log("allocat tcp port failed: no free tcp port");
			} else {
				logger.log("allocat tcp port: %d", idleTcpPort.get(0));
				response.port = idleTcpPort.get(0);
				idleTcpPort.remove(0);
			}
			return response;
		}
		
		private FreePortResponse freePort(FreePortRequest request) {
			idleTcpPort.add(request.port);
			logger.log("free tcp port:%d", request.port);
			return new FreePortResponse();
		}
	}
}
