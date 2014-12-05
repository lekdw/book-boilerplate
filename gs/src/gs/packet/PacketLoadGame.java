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

import common.model.Game;
import common.model.Mail;
import common.model.Share;
import common.model.Stage;
import common.storage.AppCouchbaseImpl;

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
		public Map<Long, Mail> mails = new HashMap<Long, Mail>();
	}

	private final static int RESULT_LOAD_GAME_OK = 0;
	@SuppressWarnings("unused")
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
			
			Stage stage = new Stage();
			stage.stageId = 1;
			stage.topScore = 0;
			
			game.stages.put((long)stage.stageId, stage);
			
			AppCouchbaseImpl.get().setGame(request.channelId, game);
		} else {
		}
		
		response.game = game;

		response.result = RESULT_LOAD_GAME_OK;
		response.now = System.currentTimeMillis();
		
		return true;
	}

	@Override
	public ByteBuf createResponse(ChannelHandlerContext ctx) throws IOException {
		return response.create(ctx);
	}
}