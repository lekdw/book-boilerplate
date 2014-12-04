package common.network;

import io.netty.channel.ChannelHandler;

public interface AppNettyServerHandler {
	public ChannelHandler getChannelHander();
	public void onNettyServerStart();
	public void onNettyServerStop();
	public void onNettyServerError();
}