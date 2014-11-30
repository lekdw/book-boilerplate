package gs.packet;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import org.msgpack.annotation.Message;

import common.AppConfig;
import common.AppConfig.Info;

public class PacketGetConfig extends PacketBase {
	@Message
	public static class GetConfigRequest extends PacketRequest {
		public String channelId = null;
	}

	@Message
	public static class GetConfigResponse extends PacketResponse {
		public int result = 0;
		public long now = 0L;
		public Info info = null;
	}

	private final static int RESULT_GET_CONFIG_OK = 0;
	private final static int RESULT_GET_CONFIG_FAIL = 1;

	protected static Logger debugLogger = LoggerFactory.getLogger("rootLogger");
	
	private GetConfigRequest request = null;
	private GetConfigResponse response = new GetConfigResponse();
	
	public PacketGetConfig(GetConfigRequest request) {
		this.request = request;
	}
	
	@Override
	public boolean processRequest() {
		if (AppConfig.get().info == null) {
			response.result = RESULT_GET_CONFIG_FAIL;
			return false;
		}
		
		if (request.channelId == null) {
			response.result = RESULT_GET_CONFIG_FAIL;
			return false;
		}
		
		response.result = RESULT_GET_CONFIG_OK;
		response.now = System.currentTimeMillis();
		response.info = AppConfig.get().info;
		
		return true;
	}

	@Override
	public ByteBuf createResponse(ChannelHandlerContext ctx) throws IOException {
		return response.create(ctx);
	}
}