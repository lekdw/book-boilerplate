package gs.packet;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class PacketBase {
	public static class PacketRequest {
		public ByteBuf create() {
			ByteBufOutputStream os = new ByteBufOutputStream(Unpooled.buffer());
			
			MessagePack msgpack = new MessagePack();
			Packer packer = msgpack.createPacker(os);
			
			try {
				packer.write(this);
			} catch (IOException e) {
				return null;
			}
			
			return os.buffer();
		}
	}

	public static class PacketResponse {
		public ByteBuf create(ChannelHandlerContext ctx) throws IOException {
			ByteBufOutputStream os = new ByteBufOutputStream(ctx.alloc().buffer());
			
			MessagePack msgpack = new MessagePack();
			Packer packer = msgpack.createPacker(os);
			
			packer.write(this);
			
			return os.buffer();
		}
	}

	protected static final JsonFactory jsonFactory = new JsonFactory();
	protected static final ObjectMapper jsonMapper = new ObjectMapper(jsonFactory);
	
	public abstract boolean processRequest() throws Exception;
	public abstract ByteBuf createResponse(ChannelHandlerContext ctx) throws IOException;
}