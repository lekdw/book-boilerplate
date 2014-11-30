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
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.timeout.ReadTimeoutException;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
	@SuppressWarnings("serial")
	public static class RequestReadException extends Exception {
		public RequestReadException(String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	public static class RequestUriException extends Exception {
		public RequestUriException(String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	public static class RequestInvalidFieldException extends Exception {
		public RequestInvalidFieldException(String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	public static class ResponseNullException extends Exception {
		public ResponseNullException(String message) {
			super(message);
		}
	}

	private static final Logger debuglogger = LoggerFactory.getLogger("rootLogger");
	private static final Logger errorLogger = LoggerFactory.getLogger("error");

	private String uri = "";
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof DefaultFullHttpRequest)) {
			ctx.close().sync();
		}
		
		HttpRequest request = (HttpRequest) msg;

		if (!request.getDecoderResult().isSuccess()) {
			ctx.close().sync();
			return;
		}

		if (request.getMethod() != HttpMethod.POST) {
			ctx.close().sync();
			return;
		}

		uri = request.getUri();

		ByteBuf content = null;

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, content.readableBytes());
        response.headers().set(LOCATION, uri);
		
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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
		else if (cause instanceof RequestInvalidFieldException) {
			errorLogger.error("{\"action\":\"{}\", \"tag\":\"exception\", \"result\":\"RequestInvalidFieldException\"}", uriName);
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