package common;
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
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

@SuppressWarnings("unused")
public class AppConfiguration {
	private static final Logger systemLogger = LoggerFactory.getLogger("system");
	private static final AppConfiguration instance = new AppConfiguration();

	private String propertyFile = "";
	
	public int serverId = 0;
	public String serverName = "";
	public int serverVersion = 0;
	public String serverIp = "";
	public int serverPort = 0;
	public int bossThreadCount = 0;
	public int workerThreadCount = 0;
	public int readTimeout = 0;
	public int confRefreshTime = 0;
	public int statRefreshTime = 0;
	public String statsdIp = null;
	public Integer statsdPort = 0;
	public List<String> couchbaseAddresses = new ArrayList<String>();
	public List<String> redisAddresses = new ArrayList<String>();
	public List<Integer> redisPorts = new ArrayList<Integer>();
	
	public static AppConfiguration get() {
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
				propertyFile = cmd.getOptionValue("p");
				
				PropertiesConfiguration	config = new PropertiesConfiguration(propertyFile);

				serverId = config.getInt("serverId");
				serverName = config.getString("serverName");
				serverVersion = config.getInt("serverVersion");
				serverIp = config.getString("serverIp");
				serverPort = config.getInt("serverPort");
				bossThreadCount = config.getInt("bossThreadCount", 5);
				workerThreadCount = config.getInt("workerThreadCount", 25);
				readTimeout = config.getInt("readTimeout", 2);
				confRefreshTime = config.getInt("confRefreshTime", 10);
				statRefreshTime = config.getInt("statRefreshTime", 3);
				statsdIp = config.getString("statsdIp");
				statsdPort = config.getInt("statsdPort");

				int num = 1;
				
				while (true) {
					String url = config.getString("couchbaseUrl." + num);

					if (url == null)
						break;
				
					couchbaseAddresses.add(String.valueOf(url));
					
					++num;
				}
				
				num = 1;

				while (true) {
					String ip = config.getString("redisIp." + num);
					int port = config.getInt("redisPort." + num, 0);

					if (ip == null)
						break;
					
					redisAddresses.add(String.valueOf(ip));
					redisPorts.add(port);
					
					++num;
				}

				return true;
			}
		} catch (Exception e) {
			systemLogger.error(e.getMessage());
			return false;
		}
		
		return false;
	}
}