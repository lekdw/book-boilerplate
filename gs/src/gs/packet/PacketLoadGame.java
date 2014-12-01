package gs.packet;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import org.msgpack.annotation.Message;

import common.AppCouchbaseImpl;
import common.model.Game;
import common.model.Share;
import common.model.Stage;

public class PacketLoadGame extends PacketBase {
	@Message
	public static class LoadGameRequest extends PacketRequest {
		public String channelId = "";
		public String nickName = "";
		public int marketId = 0;
		public String pushId = "";
	}

	@Message
	public static class LoadGameResponse extends PacketResponse {
		public int result = 0;
		public long now = 0L;
		public Game game = null;
		public Share share = null;
		public Map<Integer, Stage> stages = new HashMap<Integer, Stage>();
	}

	private final static int RESULT_LOAD_GAME_OK = 0;
	private final static int RESULT_LOAD_GAME_FAIL = 1;

	protected static Logger debugLogger = LoggerFactory.getLogger("rootLogger");
	
	private LoadGameRequest request = null;
	private LoadGameResponse response = new LoadGameResponse();
	
	final long now = System.currentTimeMillis();
	
	public PacketLoadGame(LoadGameRequest request) {
		this.request = request;
	}
	
	@Override
	public boolean processRequest() {
		Game game = AppCouchbaseImpl.get().getGame(request.channelId);
		
		if (game == null) {
			game = new Game();
			game.channelId = request.channelId;
			game.nickName = request.nickName;
			game.marketId = request.marketId;
			game.pushId = request.pushId;
			game.selStageId = 0;
			game.loadDate = new Date(now);
			game.createDate = new Date(now);

			AppCouchbaseImpl.get().setGame(request.channelId, game);
		} else {
		}

		response.result = RESULT_LOAD_GAME_OK;
		response.now = System.currentTimeMillis();
		
		return true;
	}

	@Override
	public ByteBuf createResponse(ChannelHandlerContext ctx) throws IOException {
		return response.create(ctx);
	}
}