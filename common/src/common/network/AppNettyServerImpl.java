package common.network;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppNettyServerImpl {
	private static final Logger systemLogger = LoggerFactory.getLogger("system");
	private static final AppNettyServerImpl instance = new AppNettyServerImpl();
	
	public static AppNettyServerImpl get() {
		return instance;
	}
	
	public void start(AppNettyServerHandler serverHandler, String ip, int port, int bossThreadCount, int workerThreadCount) {
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
			
			serverHandler.onNettyServerStart();
			
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