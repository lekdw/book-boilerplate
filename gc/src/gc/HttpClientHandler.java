package gc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.timeout.ReadTimeoutException;

import org.msgpack.MessagePack;

import common.AppImpl.RequestReadException;
import common.AppImpl.RequestUriException;
import common.AppImpl.ResponseNullException;
import gs.packet.PacketGetConfig.GetConfigResponse;
import gs.packet.PacketLoadGame.LoadGameResponse;

@Sharable
public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final Logger errorLogger = LoggerFactory.getLogger("error");
	
	private String uri = "";

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof DefaultHttpResponse) {
			DefaultHttpResponse response = (DefaultHttpResponse)msg;
			uri = response.headers().get(LOCATION);
			msg = null;
		}
		
		if (msg instanceof DefaultHttpContent) {
			DefaultHttpContent httpContent = (DefaultHttpContent)msg;
			ByteBuf content = httpContent.content();
			
			try {
				MessagePack msgpack = new MessagePack();

				if (uri.equalsIgnoreCase("/getconfig")) {
					@SuppressWarnings("unused")
					GetConfigResponse response = msgpack.read(content.nioBuffer(), GetConfigResponse.class);
					response = null;
				} else if (uri.equalsIgnoreCase("/loadgame")) {
					@SuppressWarnings("unused")
					LoadGameResponse response = msgpack.read(content.nioBuffer(), LoadGameResponse.class);
					response = null;
				}
			} catch (Exception e) {
				throw new RequestReadException(e.getMessage());
			}
			
			content.release();
			content = null;
			
			msg = null;
			
			ctx.close().sync();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	throws Exception {
		ctx.close().sync();
		
		String uriName = uri.replace("/", "");

		if (cause instanceof IOException) {
			errorLogger.debug("{\"action\":\"{}\", \"tag\":\"exception\", \"result\":\"IOException\"}", uriName);
		}
		else if (cause instanceof ReadTimeoutException) {
			errorLogger.debug("{\"action\":\"{}\", \"tag\":\"exception\", \"result\":\"ReadTimeoutException\"}", uriName);
		}
		else if (cause instanceof RequestReadException) {
			errorLogger.error("{\"action\":\"{}\", \"tag\":\"exception\", \"result\":\"RequestReadException\"}", uriName);
		}
		else if (cause instanceof RequestUriException) {
			errorLogger.error("{\"action\":\"{}\", \"tag\":\"exception\", \"result\":\"RequestUriException\"}", uriName);
		}
		else if (cause instanceof ResponseNullException) {
			errorLogger.error("{\"action\":\"{}\", \"tag\":\"exception\", \"result\":\"ResponseNullException\"}", uriName);
		} else {
			if (cause != null) {
				String name = cause.getClass().getName();
				String message = cause.getMessage();
				
				if (name == null)
					name = "No name";

				if (message == null)
					message = "No message";
				
				errorLogger.error("{\"action\":\"{}\", \"tag\":\"exception\", \"result\":\"{}\"}", uriName, name + ", " + message);
			} else {
				errorLogger.error("{\"action\":\"{}\", \"tag\":\"exception\", \"result\":\"Unknown.\"}");
			}
		}
	}
}