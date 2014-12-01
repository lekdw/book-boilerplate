package common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import common.AppConfig.Info.NodeInfo;

public class AppRedisImpl {
	private static final Logger systemLogger = LoggerFactory.getLogger("system");
	private static final AppRedisImpl instance = new AppRedisImpl();
	
	private int shardCount = 0;
	private JedisPool[] pool = null;
	
	public static AppRedisImpl get() {
		return instance;
	}
	
	public void start() throws Exception {
		if (pool != null)
			throw new Exception("Redis service already started.");
		
		shardCount = AppConfig.get().info.redis.nodes.length;
		
		pool = new JedisPool[shardCount];
		
		systemLogger.info("Starting Redis service.");
		
		for (int i = 0; i < shardCount; i++) {
			NodeInfo node = AppConfig.get().info.redis.nodes[i];
			
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(AppConfig.get().info.redis.connsPerNode);
			config.setMaxIdle(AppConfig.get().info.redis.connsPerNode);

			pool[i] = new JedisPool(config, node.ip, node.port);

			Jedis jedis = null;
			
			try {
				jedis = pool[i].getResource();
			} catch (JedisConnectionException ex) {
				if (jedis != null) {
					pool[i].returnBrokenResource(jedis);
					jedis = null;
				}

				systemLogger.error("Can't start Redis service.");
				
				throw ex;
			} finally {
				if (jedis != null) {
					pool[i].returnResource(jedis);
				}
			}	
		}

		systemLogger.info("Redis service started.");
	}
	
	public void stop() {
		systemLogger.info("Stopping Redis service.");

		if (pool != null)
			for (int i = 0; i < shardCount; i++)
				if (pool[i] != null)
					pool[i].destroy();
	}
	
	public JedisPool getPool(int shard) {
		return pool[shard];
	}

	public void subscribe(int shard, JedisPubSub jedisPubSub, String channel) {
		Jedis jedis = null;

		try {
			jedis = pool[shard].getResource();
			
			if (jedis != null) {
				jedis.subscribe(jedisPubSub, channel);
			}
		} catch (JedisConnectionException e) {
			if (jedis != null) {
				pool[shard].returnBrokenResource(jedis);
				jedis = null;
			}
		} finally {
			if (jedis != null) {
				pool[shard].returnResource(jedis);
			}
		}	
	}

	public boolean publish(int shard, String channel, String message) {
		Jedis jedis = null;

		try {
			jedis = pool[shard].getResource();
			
			if (jedis != null) {
				jedis.publish(channel, message);
				return true;
			}
		} catch (JedisConnectionException e) {
			if (jedis != null) {
				pool[shard].returnBrokenResource(jedis);
				jedis = null;
			}
		} finally {
			if (jedis != null) {
				pool[shard].returnResource(jedis);
			}
		}
		
		return false;
	}
}