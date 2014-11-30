package gc;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

public class HttpClientInitializer extends ChannelInitializer<SocketChannel> {
	private static final HttpClientHandler handler = new HttpClientHandler();
	
	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();

		p.addLast("codec", new HttpClientCodec());
		p.addLast("handler", handler);
	}
}