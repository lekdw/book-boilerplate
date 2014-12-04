package gs;

import io.netty.channel.ChannelHandler;
import common.AppImpl;
import common.network.AppNettyServerHandler;
import common.storage.AppCouchbaseHandler;
import common.storage.AppMySQLHandler;
import common.storage.AppRedisHandler;

public class App extends AppImpl implements AppCouchbaseHandler, AppMySQLHandler, AppRedisHandler, AppNettyServerHandler {
	private static final App theApp = new App();
	
	//
	// AppCouchbaseHandler 구현
	//
	@Override
	public void onCouchbaseStart() {
	}

	@Override
	public void onCouchbaseStop() {
	}

	@Override
	public void onCouchbaseError() {
	}
	
	//
	// AppCouchbaseHandler 구현
	//
	@Override
	public void onMySQLStart() {
	}

	@Override
	public void onMySQLStop() {
	}

	@Override
	public void onMySQLError() {
	}

	//
	// AppRedisHandler 구현
	//
	@Override
	public void onRedisStart() {
	}

	@Override
	public void onRedisStop() {
	}

	@Override
	public void onRedisError() {
	}

	@Override
	public void onRedisMessage(String channel, String message) {
	}

	//
	// AppHttpServerHandler 구현
	//
	@Override
	public ChannelHandler getChannelHander() {
		return new HttpServerInitializer();
	}

	@Override
	public void onNettyServerStart() {
	}

	@Override
	public void onNettyServerStop() {
	}

	@Override
	public void onNettyServerError() {
	}
	
	public static void main(String[] args) {
		theApp.start(args);
	}
}