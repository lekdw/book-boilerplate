package gs;
import io.netty.channel.ChannelHandler;
import common.AppHttpServerHandler;
import common.AppImpl;

public class App extends AppImpl implements AppHttpServerHandler {
	private static final App theApp = new App();
	
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