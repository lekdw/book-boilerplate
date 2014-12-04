package common.storage;

import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.*;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.*;

import common.AppConfig;

public class AppMySQLImpl {
	private static final int SHARD_COUNT = 10;
	
	private static final Logger systemLogger = LoggerFactory.getLogger("system");
	private static final AppMySQLImpl instance = new AppMySQLImpl();
	
	private SessionFactory[] sessionFactories = new SessionFactory[SHARD_COUNT];

	public static AppMySQLImpl get() {
		return instance;
	}
	
	public void start() throws Exception {
		try {
			systemLogger.info("Starting MySQL service.");
			
			int configCount = AppConfig.get().info.mysql.sessionConfigs.length;

			SessionFactory[] factories = new SessionFactory[configCount];
			
			for (int i = 0; i < configCount; i++) {
				String config = AppConfig.get().info.mysql.sessionConfigs[i];
				
				Configuration configuration = new Configuration().configure(config);
				final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
			    
			    factories[i] = configuration.buildSessionFactory(serviceRegistry);
			}

			for (int i = 0; i < SHARD_COUNT; i++) {
				int factoryIndex = i % configCount;
				sessionFactories[i] = factories[factoryIndex];
			}

			systemLogger.info("MySQL service started.");
		} catch (Exception e) {
			systemLogger.error("Can't start MySQL service.", e);
			throw e;
		}
	}
	
	public void stop() {
		systemLogger.info("Stopping MySQL service.");

		for (int i = 0; i < SHARD_COUNT; i++) {
			if (sessionFactories[i] != null) {
				sessionFactories[i].close();
				sessionFactories[i] = null;
			}
		}
		
		sessionFactories = null;
	}

	public SessionFactory getSessionFactory(int shard) {
		if (shard < 0 || shard >= SHARD_COUNT)
			return null;
		
		return sessionFactories[shard];
	}
}