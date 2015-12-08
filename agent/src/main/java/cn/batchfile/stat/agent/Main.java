package cn.batchfile.stat.agent;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
	public static int port = 9091;
	
	public static void main(String[] args) throws Exception {
		// parse args
		String pid_file = ArgumentUtils.getArgument(args, "pid-file", "f");
		String dir = ArgumentUtils.getArgument(args, "dir", "d");
		String port = ArgumentUtils.getArgument(args, "port", "p");

		//写pid文件
		String pid = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(pid_file) && !StringUtils.equals(pid_file, "null")) {
			pid = write_pid(pid_file);
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
		
		LOG.info(String.format("---- start agent, pid: %s ----", pid));
		start();
	}
	
	private static void start() throws Exception {
		//start jetty server
		Server server = new Server(port);
		setThreadCount(server);
		ServletContextHandler handler = new ServletContextHandler();
		
		// init spring
		handler.setMaxFormContentSize(Integer.MAX_VALUE);
		handler.setInitParameter("contextConfigLocation", "classpath*:spring/agent/*.xml");
		ContextLoaderListener listener = new ContextLoaderListener();
		handler.addEventListener(listener);
		
		// spring mvc
		ServletHolder sh = new ServletHolder("appServlet", DispatcherServlet.class);
		sh.setAsyncSupported(true);
		sh.setInitParameter("contextConfigLocation", "classpath:spring/agent/appServlet/servlet-context.xml");
		handler.addServlet(sh, "/");
		
		server.setStopTimeout(1000 * 20);
		server.setStopAtShutdown(true);
		server.setHandler(handler);
		server.start();
		LOG.info("Jetty was started on port: " + port);
	}
	
	public static void setThreadCount(Server server) {
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
	
	private static String write_pid(String file) throws IOException {
		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		String pid = processName.split("@")[0];
		File pid_file = new File(file);
		if (!pid_file.exists()) {
			File dir = new File(pid_file.getParent());
			if (!dir.exists()) {
				FileUtils.forceMkdir(dir);
			}
			pid_file.createNewFile();
		}
		FileUtils.writeStringToFile(pid_file, pid);
		return pid;
	}
}
