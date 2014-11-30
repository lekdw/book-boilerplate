package common;

import io.netty.channel.ChannelHandler;

public interface AppHttpServerHandler {
	abstract public ChannelHandler getChannelHander();
	
	public void onHttpServerStart();
	public void onHttpServerStop();
	public void onHttpServerError();
}