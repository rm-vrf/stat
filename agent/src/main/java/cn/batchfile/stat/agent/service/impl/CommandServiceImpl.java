package cn.batchfile.stat.agent.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import cn.batchfile.stat.agent.service.CommandService;

public class CommandServiceImpl implements CommandService {
	private static final Logger LOG = Logger.getLogger(CommandServiceImpl.class);
	private Map<String, List<String>> outs = new ConcurrentHashMap<String, List<String>>();
	private Map<String, String> dones = new ConcurrentHashMap<String, String>();
	private Map<String, Thread> threads = new ConcurrentHashMap<String, Thread>();
	
	@Override
	public String execute(String cmd) {
		LOG.info(String.format("execute command: %s", cmd));
		final List<String> out = new ArrayList<String>();
		Commandline cl = new Commandline(cmd);
		try {
			int result = CommandLineUtils.executeCommandLine(cl, new StreamConsumer() {
				@Override
				public void consumeLine(String line) {
					LOG.debug(String.format("std out: %s", line));
					out.add(line);
				}
			}, new StreamConsumer() {
				@Override
				public void consumeLine(String line) {
					LOG.debug(String.format("err out: %s", line));
					out.add(line);
				}
			});
			LOG.info(String.format("result: %s", result));
		} catch (CommandLineException e) {
			throw new RuntimeException(String.format("error when execute command: %s", cmd));
		}
		return StringUtils.join(out, IOUtils.LINE_SEPARATOR);
	}

	@Override
	public String start(final String cmd) {
		LOG.info(String.format("start command: %s", cmd));
		final String id = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
		final List<String> out = new ArrayList<String>();
		outs.put(id, out);
		final Commandline cl = new Commandline(cmd);
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int result = CommandLineUtils.executeCommandLine(cl, new StreamConsumer() {
						@Override
						public void consumeLine(String line) {
							LOG.debug(String.format("std out: %s", line));
							out.add(line);
						}
					}, new StreamConsumer() {
						@Override
						public void consumeLine(String line) {
							LOG.debug(String.format("err out: %s", line));
							out.add(line);
						}
					});
					LOG.info(String.format("result: %s", result));
				} catch (CommandLineException e) {
					LOG.error(String.format("error when execute command: %s", cmd));
				} finally {
					if (!dones.containsKey(id)) {
						dones.put(id, "done");
					}
				}
			}
		});
		threads.put(id, t);
		t.start();
		
		return id;
	}

	@Override
	public String consume(String id) {
		List<String> out = outs.get(id);
		if (out == null || (out.size() == 0 && dones.containsKey(id))) {
			return null;
		}
		
		List<String> tmp = new ArrayList<String>();
		tmp.addAll(out);
		
		for (int i = 0; i < tmp.size(); i ++) {
			out.remove(0);
		}
		return StringUtils.join(tmp, IOUtils.LINE_SEPARATOR);
	}

	@Override
	public void terminate(String id) {
		Thread t = threads.get(id);
		if (t != null) {
			try {t.interrupt();} catch (Exception e) {}
			//try {t.join();} catch (Exception e) {}
		}
		dones.put(id, "terminated");
	}
	
	@Override
	public String getState(String id) {
		if (dones.containsKey(id)) {
			return dones.get(id);
		} else if (outs.containsKey(id)) {
			return "running";
		} else {
			return "unknown";
		}
	}
	
	class CommandLocale {
		private String id;
		private Thread thread;
		private String status = "running";
		private List<String> lines = new ArrayList<String>();
		private long beginTime;
		private long lastReadTime;
		private long lastWriteTime;
		
		public CommandLocale(String id) {
			this.id = id;
			beginTime = new Date().getTime();
			lastWriteTime = beginTime;
			lastReadTime = beginTime;
			status = "running";
		}
		
		public void writeLine(String line) {
			lines.add(line);
			lastWriteTime = new Date().getTime();
		}
		
		public List<String> readLines() {
			//TODO
			return null;
		}
		
		public void terminate() {
			
		}
	}
}
