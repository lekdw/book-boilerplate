package common;

import java.net.URI;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppHttpClientImpl {
	public static interface AppHttpClientHandler {
		public void onCompleteConnect();
	};
	
	private static final Logger systemLogger = LoggerFactory.getLogger("system");
	
	EventLoopGroup group = new NioEventLoopGroup();
	Bootstrap bootstrap = new Bootstrap();
	
	ChannelHandler handler = null;
	
	public void init(Class<?> httpClientinitializerClass) throws Exception {
		handler = (ChannelHandler)httpClientinitializerClass.getConstructor().newInstance();
		bootstrap.group(group).channel(NioSocketChannel.class).handler(handler);
	}
	
	public void destroy() {
		group.shutdownGracefully();
		
		bootstrap = null;
		group = null;
	}

	public void request(URI uri, ByteBuf buf) {
		String host = uri.getHost();
		int port = uri.getPort();
		
		try {
			Channel ch = bootstrap.connect(host, port).sync().channel();
			
			DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath(), buf);
		    request.headers().set(HttpHeaders.Names.HOST, host);
		    request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
		    request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));

		    ch.writeAndFlush(request);
		    ch.closeFuture().sync();
		    
		    request.release();
		    request = null;

		    ch = null;
		} catch (Exception ex) {
			systemLogger.error(ex.getMessage());
		}
	}
}