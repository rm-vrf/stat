package cn.batchfile.stat.agent.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	private Map<String, CommandLocal> locals = new ConcurrentHashMap<String, CommandLocal>();
	
	public void init() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					long now = new Date().getTime();
					for (CommandLocal cl : locals.values()) {
						cl.timeout(now);
					}
					Thread.sleep(1000);
				} catch (Exception e) {}
				
			}
		}).start();
	}

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
		final CommandLocal cl = new CommandLocal();
		locals.put(id, cl);
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int result = CommandLineUtils.executeCommandLine(new Commandline(cmd), new StreamConsumer() {
						@Override
						public void consumeLine(String line) {
							LOG.debug(String.format("std out: %s", line));
							cl.write(line);
						}
					}, new StreamConsumer() {
						@Override
						public void consumeLine(String line) {
							LOG.debug(String.format("err out: %s", line));
							cl.write(line);
						}
					});
					LOG.info(String.format("result: %s", result));
				} catch (CommandLineException e) {
					LOG.error(String.format("error when execute command: %s", cmd));
				} finally {
					cl.finish();
				}
			}
		});
		
		cl.thread(t);
		t.start();
		
		return id;
	}

	@Override
	public String consume(String id) {
		CommandLocal cl = locals.get(id); 
		if (cl == null) {
			return null;
		}
		
		List<String> lines = cl.read();
		if (cl.status() != CommandStatus.running 
				&& lines.size() == 0) {
			return null;
		}
		
		return StringUtils.join(lines, IOUtils.LINE_SEPARATOR);
	}

	@Override
	public void terminate(String id) {
		CommandLocal cl = locals.get(id); 
		if (cl != null) {
			cl.terminate();
		}
	}
	
	@Override
	public String getStatus(String id) {
		CommandLocal cl = locals.get(id); 
		if (cl == null) {
			return "unknown";
		} else {
			return cl.status().name();
		}
	}
	
	enum CommandStatus {
		running,
		error,
		finish,
		timeout,
		terminate
	}
	
	class CommandLocal {
		private static final int TIMEOUT = 3600000;
		private static final int BUFFER_LINES = 512;
		
		private Thread thread;
		private CommandStatus status;
		private ConcurrentLinkedQueue<String> buffer;
		private long last_read_time;
		private long last_write_time;
		
		public CommandLocal() {
			long now = new Date().getTime();
			last_write_time = now;
			last_read_time = now;
			status = CommandStatus.running;
			buffer = new ConcurrentLinkedQueue<String>();
		}
		
		public void thread(Thread thread) {
			this.thread = thread;
		}
		
		public void write(String line) {
			if (buffer.size() > BUFFER_LINES) {
				buffer.remove();
			}
			buffer.add(line);
			last_write_time = new Date().getTime();
		}
		
		public List<String> read() {
			List<String> list = new ArrayList<String>();
			while (buffer.size() > 0) {
				list.add(buffer.poll());
			}
			last_read_time = new Date().getTime();
			return list;
		}
		
		public void finish() {
			if (status == CommandStatus.running) {
				status = CommandStatus.finish;
			}
		}
		
		public void error() {
			if (status == CommandStatus.running) {
				status = CommandStatus.error;
			}
		}
		
		public void terminate() {
			if (thread != null) {
				try {
					thread.interrupt();
				} catch (Exception e) {}
				thread = null;
			}
			if (status == CommandStatus.running) {
				status = CommandStatus.terminate;
			}
		}
		
		public CommandStatus status() {
			return status;
		}
		
		public boolean timeout(long now) {
			if (now - last_read_time > TIMEOUT 
					&& now - last_write_time > TIMEOUT) {
				
				LOG.info(String.format("command timeout: %s", thread.getName()));
				if (thread != null) {
					try {
						thread.interrupt();
					} catch (Exception e) {}
					thread = null;
				}
				
				buffer.clear();
				if (status == CommandStatus.running) {
					status = CommandStatus.timeout;
				}
				
				return true;
			} else {
				return false;
			}
		}
	}
}
