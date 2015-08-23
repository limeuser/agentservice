package service.os;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import service.os.msg.*;

import mjoys.frame.TLV;
import mjoys.frame.TV;
import mjoys.io.ByteBufferInputStream;
import mjoys.io.SerializerException;
import mjoys.util.Address;
import mjoys.util.Logger;
import cn.oasistech.agent.AgentProtocol;
import cn.oasistech.agent.client.AgentAsynRpc;
import cn.oasistech.agent.client.AgentRpcHandler;

public class OSServer {
	private AgentAsynRpc rpc;
	private List<Integer> idleTcpPort = new LinkedList<Integer>();
	private final static Logger logger = new Logger().addPrinter(System.out);
	
	public void start(Address serverAddress)	{
		rpc = new AgentAsynRpc();
		rpc.start(serverAddress, new Handler(rpc));
	}
	
	public class Handler implements AgentRpcHandler<ByteBuffer> {
		private AgentAsynRpc rpc;
		public Handler(AgentAsynRpc rpc) {
			this.rpc = rpc;
		}
		
		@Override
		public void handle(AgentAsynRpc rpc, TLV<ByteBuffer> frame) {
			int requester = frame.tag;
			TV<ByteBuffer> request = AgentProtocol.parseMsgFrame(frame.body);
			MsgType msgType = MsgType.values()[request.tag];
			try {
				processRequest(requester, msgType, request);
			} catch (Exception e) {
				logger.log("decode request exception", e);
			}
		}
		
		private void processRequest(int requester, MsgType msgType, TV<ByteBuffer> request) throws SerializerException, IOException {
			switch (msgType) {
			case AllocatePort:
				rpc.sendMsg(requester, msgType.ordinal(), allocatePort(rpc.getSerializer().decode(new ByteBufferInputStream(request.body), AllocatePortRequest.class)));
				break;
			case FreePort:
				freePort(rpc.getSerializer().decode(new ByteBufferInputStream(request.body), FreePortRequest.class));
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
			} else {
				response.port = idleTcpPort.get(0);
				idleTcpPort.remove(0);
			}
			return response;
		}
		
		private void freePort(FreePortRequest request) {
			idleTcpPort.add(request.port);
		}
	}
}
