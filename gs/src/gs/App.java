package gs;

import io.netty.channel.ChannelHandler;
import common.AppCouchbaseHandler;
import common.AppHttpServerHandler;
import common.AppImpl;
import common.AppRedisHandler;

public class App extends AppImpl implements AppCouchbaseHandler, AppRedisHandler, AppHttpServerHandler {
	private static final App theApp = new App();

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
	// AppHttpServerHandler 구현
	//
	@Override
	public ChannelHandler getChannelHander() {
		return new HttpServerInitializer();
	}

	@Override
	public void onHttpServerStart() {
	}

	@Override
	public void onHttpServerStop() {
	}

	@Override
	public void onHttpServerError() {
	}
	
	public static void main(String[] args) {
		theApp.start(args);
	}
}