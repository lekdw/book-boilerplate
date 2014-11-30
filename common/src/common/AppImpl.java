package common;

public abstract class AppImpl {
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
		AppConfiguration.get().readFile(args);
		
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
							AppConfiguration.get().serverIp,
							AppConfiguration.get().serverPort,
							AppConfiguration.get().bossThreadCount,
							AppConfiguration.get().workerThreadCount);
				}
			}).start();
		}
		
		if (isFailed)
			return false;

		return true;
	}

	public boolean stop() {
		return true;
	}
}