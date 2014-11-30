package common;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppHttpServerImpl {
	private static final Logger systemLogger = LoggerFactory.getLogger("system");
	private static final AppHttpServerImpl instance = new AppHttpServerImpl();
	
	public static AppHttpServerImpl get() {
		return instance;
	}
	
	public void start(AppHttpServerHandler serverHandler, String ip, int port, int bossThreadCount, int workerThreadCount) {
		systemLogger.info("Ip, Port, Boss, Worker... {}, {}, {}, {}", ip, port, bossThreadCount, workerThreadCount);

		EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadCount);
		EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadCount);
		
		ServerBootstrap bootstrap = new ServerBootstrap();

		try {
			ChannelHandler handler = serverHandler.getChannelHander();
			
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(handler);
			
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			bootstrap.option(ChannelOption.ALLOW_HALF_CLOSURE, true);
			bootstrap.option(ChannelOption.SO_LINGER, 0);

			Channel ch = bootstrap.bind(ip, port).sync().channel();
			
			serverHandler.onHttpServerStart();
			
			ch.closeFuture().sync();
		} catch (Exception ex) {
			systemLogger.error(ex.getMessage());
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public void stop() {
	}
}