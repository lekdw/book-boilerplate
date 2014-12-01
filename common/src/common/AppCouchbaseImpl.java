package common;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.spy.memcached.internal.GetCompletionListener;
import net.spy.memcached.internal.OperationCompletionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.ViewDesign;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.model.Game;

public class AppCouchbaseImpl {
	private static final Logger systemLogger = LoggerFactory.getLogger("system");
	private static final ObjectMapper jsonMapper = new ObjectMapper();
	private static final AppCouchbaseImpl instance = new AppCouchbaseImpl();
	
	private CouchbaseClient client = null;
	
	public static AppCouchbaseImpl get() {
		return instance;
	}
	
	public void start() throws Exception {
		if (client != null)
			throw new Exception("Couchbase service already started.");

		try {
			systemLogger.info("Starting Couchbase service.");
			
			List<URI> hosts = new ArrayList<URI>();
			
			for (String url : AppConfig.get().info.couchbase.urls)
				hosts.add(new URI(url));
			
			String password = "";
			
			CouchbaseConnectionFactoryBuilder builder = new CouchbaseConnectionFactoryBuilder();
			builder.setOpTimeout(AppConfig.get().info.couchbase.opTimeout);
			builder.setViewTimeout(AppConfig.get().info.couchbase.viewTimeout);
			builder.setViewWorkerSize(AppConfig.get().info.couchbase.viewWorkerSize);
			builder.setViewConnsPerNode(AppConfig.get().info.couchbase.viewConnsPerNode);
			
			client = new CouchbaseClient(builder.buildCouchbaseConnection(hosts, AppConfig.get().info.couchbase.bucket, password));
			
			systemLogger.info("Couchbase service started.");
		} catch (Exception ex) {
			systemLogger.error("Can't start Couchbase service.", ex);
			throw ex;
		}
	}
	
	public void stop() {
		systemLogger.info("Stopping Couchbase service.");
		
		if (client != null)
			client.shutdown();
	}
	
	public boolean hasView(String name) {
		DesignDocument designDoc = new DesignDocument(AppConfig.get().info.couchbase.designDoc);
		
		for (ViewDesign viewDesign : designDoc.getViews())
			if (viewDesign.getName().equalsIgnoreCase(name))
				return true;
				
		return false;
	}
	
	public boolean setGame(String channelId, Game game) {
		String value = null;
		
		try {
			value = jsonMapper.writeValueAsString(game);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		}
		
		if (value == null)
			return false;
		
		try {
			return client.set("user:" + channelId, value).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public boolean setGameAsync(String channelId, Game game, OperationCompletionListener listener) {
		String value = null;
		
		try {
			value = jsonMapper.writeValueAsString(game);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		}
		
		if (value == null)
			return false;
		
		client.set("user:" + channelId, value).addListener(listener);
		
		return true;
	}

	public Game getGame(String channelId) {
		String value = (String)client.get("user:" + channelId);
		
		if (value == null)
			return null;
		
		try {
			Game game = jsonMapper.readValue(value, Game.class);
			return game;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean asyncGetGame(String channelId, GetCompletionListener listener) {
		client.asyncGet("user:" + channelId).addListener(listener);
		
		return true;
	}
}