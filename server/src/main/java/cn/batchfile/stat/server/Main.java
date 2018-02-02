package cn.batchfile.stat.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
	
	public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
	
//	public static void main(String[] args) throws Exception {
//		// parse args
//		String pid_file = ArgumentUtils.getArgument(args, "pid-file", "f");
//		String dir = ArgumentUtils.getArgument(args, "dir", "d");
//		String port = ArgumentUtils.getArgument(args, "port", "p");
//		String webapp = ArgumentUtils.getArgument(args, "webapp", "w");
//
//		//write pid file
//		String pid = StringUtils.EMPTY;
//		if (StringUtils.isNotBlank(pid_file) && !StringUtils.equals(pid_file, "null")) {
//			pid = write_pid(pid_file);
//		}
//		
//		if (StringUtils.isNotEmpty(port) && !StringUtils.equals(port, "null")) {
//			Main.port = Integer.valueOf(port);
//		}
//		
//		if (StringUtils.isBlank(webapp) || StringUtils.equals(webapp, "null")) {
//			webapp = "src/main/webapp";
//		}
//		
//		// load log4j config
//		String log4j = PathUtils.concat(dir, "conf/log4j.properties");
//		if (new File(log4j).exists()) {
//			LOG.info(String.format("<load log4j config: %s>", log4j));
//			org.apache.log4j.PropertyConfigurator.configureAndWatch(log4j, 20000);
//		}
//
//		LOG.info(String.format("---- start server, pid: %s ----", pid));
//		start(Main.port, webapp);
//	}
//	
//	private static void start(int port, String webapp) throws Exception {
//		Server server = new Server(port);
//		cn.batchfile.stat.agent.Main.setThreadCount(server);
//		
//		WebAppContext webAppContext = new WebAppContext();
//		webAppContext.setMaxFormContentSize(Integer.MAX_VALUE);
//		webAppContext.setContextPath("/");
//		webAppContext.setWar(webapp);
//		webAppContext.setServer(server);
//		
//		HashLoginService loginService = new HashLoginService("STAT-SECURITY-REALM");
//		webAppContext.getSecurityHandler().setLoginService(loginService);
//		
//		server.setHandler(webAppContext);
//		server.start();
//		LOG.info("http server started, port: " + port);
//	}
//
//	private static String write_pid(String file) throws IOException {
//		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
//		String pid = processName.split("@")[0];
//		File pid_file = new File(file);
//		if (!pid_file.exists()) {
//			File dir = new File(pid_file.getParent());
//			if (!dir.exists()) {
//				FileUtils.forceMkdir(dir);
//			}
//			pid_file.createNewFile();
//		}
//		FileUtils.writeStringToFile(pid_file, pid);
//		return pid;
//	}
}
