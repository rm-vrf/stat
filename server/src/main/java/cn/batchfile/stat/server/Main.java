package cn.batchfile.stat.server;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import cn.batchfile.stat.util.ArgumentUtils;
import cn.batchfile.stat.util.PathUtils;

public class Main {
	private static final Logger LOG = Logger.getLogger(Main.class);

	public static String address = null;
	public static int port = 9090;
	
	public static void main(String[] args) throws Exception {
		// parse args
		String dir = ArgumentUtils.getArgument(args, "dir", "d");
		String address = ArgumentUtils.getArgument(args, "address", "a");
		String port = ArgumentUtils.getArgument(args, "port", "p");
		String webapp = ArgumentUtils.getArgument(args, "webapp", "w");

		if (StringUtils.isNotEmpty(address) && !StringUtils.equals(address, "null")) {
			Main.address = address;
		}
		
		if (StringUtils.isNotEmpty(port) && !StringUtils.equals(port, "null")) {
			Main.port = Integer.valueOf(port);
		}
		
		if (StringUtils.isBlank(webapp) || StringUtils.equals(webapp, "null")) {
			webapp = "src/main/webapp";
		}
		
		// load log4j config
		String log4j = PathUtils.concat(dir, "conf/log4j.properties");
		if (new File(log4j).exists()) {
			LOG.info(String.format("<load log4j config: %s>", log4j));
			org.apache.log4j.PropertyConfigurator.configureAndWatch(log4j, 20000);
		}

		// run start|stop|restart
		String command = args.length > 0 ? args[0] : null;
		if (StringUtils.equals(command, "start")) {
			start(Main.port, webapp);
		} else if (StringUtils.equals(command, "stop")) {
			stop();
		} else if (StringUtils.equals(command, "install")) {
			install();
		} else {
			print_helper();
		}
	}
	
	private static void start(int port, String webapp) throws Exception {
		LOG.info(String.format("---- start server, pid: %s ----", ""));
		Server server = new Server(port);
		cn.batchfile.stat.agent.Main.setThreadCount(server);
		
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setMaxFormContentSize(Integer.MAX_VALUE);
		webAppContext.setContextPath("/");
		webAppContext.setWar(webapp);
		webAppContext.setServer(server);
		
		HashLoginService loginService = new HashLoginService("STAT-SECURITY-REALM");
		webAppContext.getSecurityHandler().setLoginService(loginService);
		
		server.setHandler(webAppContext);
		server.start();
		LOG.info("http server started, port: " + port);
	}
	
	private static void stop() throws IOException, CommandLineException, InterruptedException {
		LOG.info("stop");
	}
	
	private static void install() throws IOException {
		LOG.info("install");
	}
	
	private static void print_helper() {
		LOG.error("Usage: ./server.sh install|start|stop");
	}
}
