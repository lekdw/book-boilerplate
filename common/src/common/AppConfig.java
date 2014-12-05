package common;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.msgpack.annotation.Message;

@SuppressWarnings("unused")
public class AppConfig {
	@Message
	static public class Info {
		@Message
		static public class NodeInfo {
			public String url = null;
			public String ip = null;
			public int port = 0;
		}

		@Message
		static public class AppInfo {
			public int id = 0;
			public String name = null;
			public String version = null;
		}

		@Message
		static public class NettyInfo {
			public String ip = null;
			public int port = 0;
			public int bossThread = 0;
			public int workerThread = 0;
			public int readTimeout = 0;
		}

		@Message
		static public class CouchbaseInfo {
			public String[] urls = null;
			public String bucket = null;
			public String designDoc = null;
			public int opTimeout = 0;
			public int viewTimeout = 0;
			public int viewWorkerSize = 0;
			public int viewConnsPerNode = 0;
		}

		@Message
		static public class MySQLInfo {
			public String[] sessionConfigs = null;
		}

		@Message
		static public class RedisInfo {
			public NodeInfo[] nodes = null;
			public int connsPerNode = 0;
			public String messageChannel = null;
		}
		
		public AppInfo app = null;
		public NettyInfo netty = null;
		public CouchbaseInfo couchbase = null;
		public MySQLInfo mysql = null;
		public RedisInfo redis = null;
	}
	
	private static final Logger systemLogger = LoggerFactory.getLogger("system");
	private static final AppConfig instance = new AppConfig();

	public Info info;	
	
	public static AppConfig get() {
		return instance;
	}
	
	public void init() {
	}
	
	public boolean readFile(String[] args) {
		Options options = new Options();
		options.addOption("h", "help", false, "options");
		options.addOption("p", "property", true, "set property files");
		
		CommandLineParser parser = new PosixParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("gs", options);
				return false;
			}
			
			if (cmd.hasOption("p")) {
				String propertyFile = cmd.getOptionValue("p");
				
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

				info = mapper.readValue(new File(propertyFile), Info.class);

				return true;
			}
		} catch (Exception e) {
			systemLogger.error(e.getMessage());
			return false;
		}
		
		return false;
	}
}