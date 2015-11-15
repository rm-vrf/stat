package cn.batchfile.stat.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

import cn.batchfile.stat.util.ArgumentUtils;
import cn.batchfile.stat.util.PathUtils;

public class Main {
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	public static String address = null;
	public static int port = 9091;
	
	public static void main(String[] args) throws Exception {
		// parse args
		String dir = ArgumentUtils.getArgument(args, "dir", "d");
		String address = ArgumentUtils.getArgument(args, "address", "a");
		String port = ArgumentUtils.getArgument(args, "port", "p");

		if (StringUtils.isNotEmpty(address) && !StringUtils.equals(address, "null")) {
			Main.address = address;
		}
		
		if (StringUtils.isNotEmpty(port) && !StringUtils.equals(port, "null")) {
			Main.port = Integer.valueOf(port);
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
			start();
		} else if (StringUtils.equals(command, "stop")) {
			stop();
		} else if (StringUtils.equals(command, "install")) {
			install();
		} else {
			print_helper();
		}
	}
	
	private static void start() throws Exception {
		//check config file
		Properties properties = read_config();
		if (!properties.containsKey("agent.id")) {
			LOG.error("Must run install before start.");
			print_helper();
			return;
		}
		
		//write pid file
		String pid = write_pid();
		LOG.info(String.format("---- start agent, pid: %s ----", pid));

		//start jetty server
		Server server = new Server(port);
		setThreadCount(server);
		ServletContextHandler handler = new ServletContextHandler();
		
		// init spring
		handler.setMaxFormContentSize(Integer.MAX_VALUE);
		handler.setInitParameter("contextConfigLocation", "classpath*:spring/*.xml");
		ContextLoaderListener listener = new ContextLoaderListener();
		handler.addEventListener(listener);
		
		// spring mvc
		ServletHolder sh = new ServletHolder("appServlet", DispatcherServlet.class);
		sh.setAsyncSupported(true);
		sh.setInitParameter("contextConfigLocation", "classpath:spring/appServlet/servlet-context.xml");
		handler.addServlet(sh, "/");
		
		server.setStopTimeout(1000 * 20);
		server.setStopAtShutdown(true);
		server.setHandler(handler);
		server.start();
		LOG.info("Jetty was started on port: " + port);
	}
	
	private static void setThreadCount(Server server) {
		ThreadPool pool = server.getThreadPool();
		if (pool instanceof QueuedThreadPool) {
			QueuedThreadPool qtp = (QueuedThreadPool)pool;
			
			int maxThreads = Integer.valueOf(System.getProperty("max.threads", "-1"));
			int minThreads = Integer.valueOf(System.getProperty("min.threads", "-1"));
			
			if (maxThreads > 0) {
				qtp.setMaxThreads(maxThreads);
			}
			
			if (minThreads > 0) {
				qtp.setMinThreads(minThreads);
			}
		}
	}
	
	private static void stop() throws IOException, CommandLineException, InterruptedException {
		//get pid
		final String pid = read_pid();
		
		//send ps command
		Commandline cl = new Commandline(String.format("ps -p %s", pid));
		CommandLineUtils.executeCommandLine(cl, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				line = StringUtils.trim(line);
				if (StringUtils.startsWith(line, pid) && StringUtils.contains(line, "java")) {
					LOG.info(String.format("stop agent, id: %s", pid));
					try {
						CommandLineUtils.executeCommandLine(new Commandline(String.format("kill -15 %s", pid)), null, null);
						int i = 0;
						while (CommandLineUtils.isAlive(Long.valueOf(pid)) && i ++ < 20) {
							Thread.sleep(500);
						}
						if (CommandLineUtils.isAlive(Long.valueOf(pid))) {
							CommandLineUtils.executeCommandLine(new Commandline(String.format("kill -9 %s", pid)), null, null);
						}
					} catch (Exception e) {
						//pass
					}
				}
			}
		}, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				LOG.error(line);
			}
		});

		int i = 0;
		while (CommandLineUtils.isAlive(Long.valueOf(pid)) && i ++ < 40) {
			Thread.sleep(500);
		}
		
		if (CommandLineUtils.isAlive(Long.valueOf(pid))) {
			throw new RuntimeException("cannot stop agent, pid: " + pid);
		}
	}
	
	private static void install() throws IOException {
		Properties properties = read_config();
		String agent_id = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
		
		// exist agent id in properties file
		if (properties.containsKey("agent.id")) {
			String replace = null;
			while (!StringUtils.equalsIgnoreCase(replace, "y") && !StringUtils.equalsIgnoreCase(replace, "n")) {
				replace = input("Replace agent id? Y/n: ", "Y");
			}
			if (StringUtils.equalsIgnoreCase(replace, "y")) {
				agent_id = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
			} else {
				agent_id = properties.getProperty("agent.id").toString();
			}
		}
		
		// write agent id to config file
		properties.put("agent.id", agent_id);
		write_config(properties);
		
		// prompt message
		output("Installation is completed.");
	}
	
	private static String write_pid() throws IOException {
		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		String pid = processName.split("@")[0];
		String data_dir = data_dir();
		FileUtils.forceMkdir(new File(data_dir));
		File pid_file = new File(PathUtils.concat(data_dir, "pid"));
		FileUtils.writeStringToFile(pid_file, pid);
		return pid;
	}
	
	private static String read_pid() throws IOException {
		String data_dir = data_dir();
		File pid_file = new File(PathUtils.concat(data_dir, "pid"));
		if (pid_file.exists()) {
			return FileUtils.readFileToString(pid_file);
		} else {
			return null;
		}
	}
	
	public static Properties read_config() throws IOException {
		String data_dir = data_dir();
		Properties props = new Properties();
		InputStream inputStream = null;
		try {
			File file = new File(PathUtils.concat(data_dir, "config.properties"));
			if (file.exists()) {
				inputStream = new FileInputStream(file);
				props.load(inputStream);
			}
			return props;
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	private static void write_config(Properties properties) throws IOException {
		String data_dir = data_dir();
		FileUtils.forceMkdir(new File(data_dir));
		File file = new File(PathUtils.concat(data_dir, "config.properties"));
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		try {
			properties.store(out, StringUtils.EMPTY);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	private static String data_dir() {
		return PathUtils.concat(FileUtils.getUserDirectoryPath(), ".stat/agent/");
	}

	private static void print_helper() {
		LOG.error("Usage: ./agent.sh install|start|stop");
	}
	
	private static String input(String message, String value) throws IOException {
		System.out.print(message);
		int read = System.in.read();
		String s = "" + (char)read;
		if (StringUtils.isBlank(s)) {
			s = value;
		}
		return s;
	}
	
	private static void output(String message) {
		System.out.println(message);
	}
}
