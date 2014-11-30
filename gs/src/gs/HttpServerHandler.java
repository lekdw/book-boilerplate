package gs;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.timeout.ReadTimeoutException;

import org.msgpack.MessagePack;

import common.AppImpl.RequestReadException;
import common.AppImpl.RequestUriException;
import common.AppImpl.ResponseNullException;

import gs.packet.PacketBase;
import gs.packet.PacketGetConfig;
import gs.packet.PacketGetConfig.GetConfigRequest;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger debuglogger = LoggerFactory.getLogger("rootLogger");
	private static final Logger errorLogger = LoggerFactory.getLogger("error");

	private static final int getconfig = "/getconfig".hashCode();
	
	private String uri = "";
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof DefaultFullHttpRequest)) {
			ctx.close().sync();
		}
		
		DefaultFullHttpRequest httpRequest = (DefaultFullHttpRequest)msg;

		if (!httpRequest.getDecoderResult().isSuccess()) {
			ctx.close().sync();
			return;
		}

		if (httpRequest.getMethod() != HttpMethod.POST) {
			ctx.close().sync();
			return;
		}

		ByteBuf requestContent = httpRequest.content();
		
		if (!requestContent.isReadable()) {
			ctx.close().sync();
			return;
		}
		
		uri = httpRequest.getUri();

		PacketBase packet = null;

		try {
			MessagePack msgpack = new MessagePack();

			int uriHashCode = uri.hashCode();

			if (uriHashCode == getconfig) {
				GetConfigRequest request = msgpack.read(requestContent.nioBuffer(), GetConfigRequest.class);
				packet = new PacketGetConfig(request);
			} else {
				throw new RequestUriException("");
			}
		} catch (Exception e) {
			throw new RequestReadException(e.getMessage());
		}

		if (!packet.processRequest()) {
		}

		ByteBuf responseContent = null;

		try {
			responseContent = packet.createResponse(ctx);
			
			if (responseContent == null)
				throw new ResponseNullException("");					
		} catch (Exception e) {
			throw e;
		}

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, responseContent);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, responseContent.readableBytes());
        response.headers().set(LOCATION, uri);
		
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		
		debuglogger.debug("{\"action\":\"{}\", \"tag\":\"response\"}", uri.replace("/", ""));
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