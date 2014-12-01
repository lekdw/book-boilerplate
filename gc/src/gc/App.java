package gc;

import gs.packet.PacketGetConfig.GetConfigRequest;
import gs.packet.PacketLoadGame.LoadGameRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import common.AppHttpClientImpl;

public class App {
	private static volatile int order = 0;

	public static void main(String[] args) throws Exception {
		int count = 1;
		ExecutorService executor = Executors.newFixedThreadPool(count);
		
		long now = System.currentTimeMillis();

		final GenericObjectPool<AppHttpClientImpl> pool = new GenericObjectPool<AppHttpClientImpl>(new PooledObjectFactory<AppHttpClientImpl>() {
			@Override
			public void activateObject(PooledObject<AppHttpClientImpl> arg0) throws Exception {
			}

			@Override
			public void destroyObject(PooledObject<AppHttpClientImpl> arg0) throws Exception {
			}

			@Override
			public PooledObject<AppHttpClientImpl> makeObject() throws Exception {
				AppHttpClientImpl client = new AppHttpClientImpl();
				client.init(HttpClientInitializer.class);
				return new DefaultPooledObject<AppHttpClientImpl>(client);
			}

			@Override
			public void passivateObject(PooledObject<AppHttpClientImpl> arg0) throws Exception {
			}

			@Override
			public boolean validateObject(PooledObject<AppHttpClientImpl> arg0) {
				return false;
			}
		});
		
		pool.setMaxTotal(count);
		pool.setMaxIdle(count);
		pool.setMinIdle(count);
		pool.setBlockWhenExhausted(true);
		
		for (int i = 0; i < count; i++) {
			final LoadGameRequest request = new LoadGameRequest();
			request.channelId = String.valueOf(now + i);
			request.nickName = "사용자";
			
			try {
				final AppHttpClientImpl client = pool.borrowObject();
				
				try {
					executor.execute(new Runnable() {
						public void run() {
							System.out.printf("%d, %d, %d\n", order++, pool.getCreatedCount(), pool.getNumActive());
		
							try {
								client.request(new URI("http://127.0.0.1:1005/loadgame"), request.create());
							} catch (URISyntaxException e) {
								e.printStackTrace();
							} finally {
								client.destroy();
								pool.returnObject(client);
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					client.destroy();
					pool.returnObject(client);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.HOURS);
	}
}