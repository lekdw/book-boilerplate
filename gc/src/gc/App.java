package gc;

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

import common.network.AppNettyClientImpl;

public class App {
	private static volatile int order = 0;

	public static void main(String[] args) throws Exception {
		int count = 1;
		ExecutorService executor = Executors.newFixedThreadPool(count);
		
		long now = System.currentTimeMillis();

		final GenericObjectPool<AppNettyClientImpl> pool = new GenericObjectPool<AppNettyClientImpl>(new PooledObjectFactory<AppNettyClientImpl>() {
			@Override
			public void activateObject(PooledObject<AppNettyClientImpl> arg0) throws Exception {
			}

			@Override
			public void destroyObject(PooledObject<AppNettyClientImpl> arg0) throws Exception {
			}

			@Override
			public PooledObject<AppNettyClientImpl> makeObject() throws Exception {
				AppNettyClientImpl client = new AppNettyClientImpl();
				client.init(HttpClientInitializer.class);
				return new DefaultPooledObject<AppNettyClientImpl>(client);
			}

			@Override
			public void passivateObject(PooledObject<AppNettyClientImpl> arg0) throws Exception {
			}

			@Override
			public boolean validateObject(PooledObject<AppNettyClientImpl> arg0) {
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
				final AppNettyClientImpl client = pool.borrowObject();
				
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