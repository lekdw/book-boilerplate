package common;

import redis.clients.jedis.JedisPubSub;

public abstract class AppImpl {
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
	public static class ResponseNullException extends Exception {
		public ResponseNullException(String message) {
			super(message);
		}
	}

	public static class RedisMessageThread extends JedisPubSub implements Runnable {
		private AppImpl theApp = null;
		
		public RedisMessageThread(AppImpl theApp) {
			this.theApp = theApp;
		}
		
		@Override
		public void onMessage(String channel, String message) {
			((AppRedisHandler)theApp).onRedisMessage(channel, message);
		}

		@Override
		public void onPMessage(String pattern, String channel, String message) {
		}

		@Override
		public void onPSubscribe(String pattern, int subscribedChannels) {
		}

		@Override
		public void onPUnsubscribe(String pattern, int subscribedChannels) {
		}

		@Override
		public void onSubscribe(String channel, int subscribedChannels) {
		}

		@Override
		public void onUnsubscribe(String channel, int subscribedChannels) {
		}
		
		@Override
		public void run() {
			// Blocked!
			AppRedisImpl.get().subscribe(0, this, AppConfig.get().info.redis.messageChannel);
		}
	}
	
	protected boolean isFailed = false;
	
	public void setFailed() {
		isFailed = true;
	}
	
	public boolean start(String[] args) {
		final AppImpl theApp = this;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				theApp.stop();
			}
		});
		
		// 실행 인자로 지정된 설정파일을 읽어들인다.
		AppConfig.get().readFile(args);
		
		// 어플리케이션이 Couchbase 속성(AppCouchbaseHandler 인터페이스)을 갖는다면
		if (this instanceof AppCouchbaseHandler) {
			System.out.println("Support Couchbase!");

			try {
				AppCouchbaseImpl.get().start();
				
				// 알림!
				((AppCouchbaseHandler)theApp).onCouchbaseStart();
				
				if (isFailed) {
					AppCouchbaseImpl.get().stop();
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				AppCouchbaseImpl.get().stop();
				return false;
			}
		}

		// 어플리케이션이 MySQL 속성(AppMySQLHandler 인터페이스)을 갖는다면
		if (this instanceof AppMySQLHandler) {
			System.out.println("Support MySQL!");

			try {
				AppMySQLImpl.get().start();
				
				// 알림!
				((AppMySQLHandler)theApp).onMySQLStart();
				
				if (isFailed) {
					AppMySQLImpl.get().stop();
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				AppMySQLImpl.get().stop();
				return false;
			}
		}

		// 어플리케이션이 Redis 속성(AppRedisHandler 인터페이스)을 갖는다면
		if (this instanceof AppRedisHandler) {
			System.out.println("Support Redis!");

			RedisMessageThread worker = null;

			try {
				AppRedisImpl.get().start();
				
				worker = new RedisMessageThread(this);
				Thread thread = new Thread(worker);
				thread.setName("RedisMessageThread");
				thread.start();
				
				// 알림!
				((AppRedisHandler)theApp).onRedisStart();

				if (isFailed) {
					// PubSub 스레드 종료
					if (worker != null)
						worker.unsubscribe();
					
					AppRedisImpl.get().stop();
					AppCouchbaseImpl.get().stop();
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();

				// PubSub 스레드 종료
				if (worker != null)
					worker.unsubscribe();
				
				AppRedisImpl.get().stop();
				AppCouchbaseImpl.get().stop();
				return false;
			}
		}
		
		// 어플리케이션이 Http 서버 속성(AppHttpServerHandler 인터페이스)을 갖는다면
		if (this instanceof AppHttpServerHandler) {
			System.out.println("Support http server!");
			
			// AppHttpServerImpl.get().start 호출은 스레드를 블록시키기 때문에 별도의
			// 스레드를 생성하여 호출한다.
			new Thread(new Runnable() {
				@Override
				public void run() {
					// Http 서버를 시작한다.
					AppHttpServerImpl.get().start((AppHttpServerHandler)theApp,
							AppConfig.get().info.httpServer.ip,
							AppConfig.get().info.httpServer.port,
							AppConfig.get().info.httpServer.bossThread,
							AppConfig.get().info.httpServer.workerThread);
				}
			}, "HttpServerThread").start();
		}
		
		if (isFailed)
			return false;

		return true;
	}

	public void stop() {
		AppHttpServerImpl.get().stop();
		AppRedisImpl.get().stop();
		AppMySQLImpl.get().stop();
		AppCouchbaseImpl.get().stop();
	}
}