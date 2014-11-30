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

@SuppressWarnings("unused")
public class AppConfig {
	static public class info {
		static public class NodeInfo {
			public String url = null;
			public String ip = null;
			public int port = 0;
		}

		static public class AppInfo {
			public int id = 0;
			public String name = null;
			public String version = null;
		}

		static public class HttpServerInfo {
			public String ip = null;
			public int port = 0;
			public int bossThread = 0;
			public int workerThread = 0;
			public int readTimeout = 0;
		}

		static public class CouchbaseInfo {
			public NodeInfo[] nodes = null;
		}

		static public class RedisInfo {
			public NodeInfo[] nodes = null;
		}
		
		public AppInfo app = null;
		public HttpServerInfo httpServer = null;
		public CouchbaseInfo couchbase = null;
		public RedisInfo redis = null;
	}
	
	private static final Logger systemLogger = LoggerFactory.getLogger("system");
	private static final AppConfig instance = new AppConfig();

	public info info;	
	
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

				info = mapper.readValue(new File(propertyFile), info.class);

				return true;
			}
		} catch (Exception e) {
			systemLogger.error(e.getMessage());
			return false;
		}
		
		return false;
	}
}