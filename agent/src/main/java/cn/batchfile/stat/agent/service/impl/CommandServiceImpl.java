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

import cn.batchfile.stat.agent.domain.Command;
import cn.batchfile.stat.agent.service.CommandService;

public class CommandServiceImpl implements CommandService {
	private static final Logger LOG = Logger.getLogger(CommandServiceImpl.class);
	private static final int TIMEOUT = 3600000;
	private Map<String, CommandLocal> locals = new ConcurrentHashMap<String, CommandLocal>();
	
	public void init() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					List<String> removes = new ArrayList<String>();
					try {
						long now = new Date().getTime();
						for (CommandLocal cl : locals.values()) {
							//stop timeout job
							if (cl.timeout(now)) {
								LOG.info(String.format("command timeout, id: %s", cl.id));
							}
							
							//remove stop job after 1 hour
							if (cl.status != CommandStatus.running && now - cl.stop_time > TIMEOUT) {
								removes.add(cl.id);
							}
						}
						
						for (String id : removes) {
							locals.remove(id);
						}
						
						Thread.sleep(1000);
					} catch (Exception e) {}
				}
			}
		}).start();
	}

	@Override
	public String execute(String cmd) {
		LOG.debug(String.format("execute command: %s", cmd));
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
			LOG.debug(String.format("result: %s", result));
		} catch (CommandLineException e) {
			throw new RuntimeException(String.format("error when execute command: %s", cmd));
		}
		return StringUtils.join(out, IOUtils.LINE_SEPARATOR);
	}

	@Override
	public String start(final String cmd, boolean background) {
		LOG.info(String.format("start command: %s", cmd));
		final String id = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
		final CommandLocal cl = new CommandLocal(id, cmd, background);
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
		
		cl.thread = t;
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
		if (cl.status != CommandStatus.running 
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
			return cl.status.name();
		}
	}
	
	@Override
	public List<Command> getCommands() {
		List<Command> list = new ArrayList<Command>();
		for (CommandLocal cl : locals.values()) {
			list.add(compose_command(cl));
		}
		return list;
	}
	
	@Override
	public Command getCommand(String id) {
		CommandLocal cl = locals.get(id);
		if (cl == null) {
			return null;
		} else {
			return compose_command(cl);
		}
	}
	
	private Command compose_command(CommandLocal cl) {
		Command c = new Command();
		c.setBackground(cl.background);
		c.setCommand(cl.command);
		c.setId(cl.id);
		c.setReadTime(cl.read_time > 0 ? new Date(cl.read_time) : null);
		c.setStartTime(cl.start_time > 0 ? new Date(cl.start_time) : null);
		c.setStatus(cl.status.name());
		c.setStopTime(cl.stop_time > 0 ? new Date(cl.stop_time) : null);
		return c;
	}

	enum CommandStatus {
		running,
		error,
		finish,
		timeout,
		terminate
	}
	
	class CommandLocal {
		private static final int BUFFER_LINES = 512;

		private String id;
		private String command;
		private Thread thread;
		private long start_time;
		private long stop_time;
		private boolean background;
		private CommandStatus status;
		private ConcurrentLinkedQueue<String> buffer;
		private long read_time;
		
		public CommandLocal(String id, String command, boolean background) {
			this.id = id;
			this.command = command;
			this.background = background;
			long now = new Date().getTime();
			start_time = now;
			stop_time = 0;
			read_time = now;
			status = CommandStatus.running;
			buffer = new ConcurrentLinkedQueue<String>();
		}
		
		public void write(String line) {
			if (buffer.size() > BUFFER_LINES) {
				buffer.remove();
			}
			buffer.add(line);
			//last_write_time = new Date().getTime();
		}
		
		public List<String> read() {
			List<String> list = new ArrayList<String>();
			while (buffer.size() > 0) {
				list.add(buffer.poll());
			}
			read_time = new Date().getTime();
			return list;
		}
		
		public void finish() {
			if (status == CommandStatus.running) {
				status = CommandStatus.finish;
			}
			stop_time = new Date().getTime();
		}
		
		public void error() {
			if (status == CommandStatus.running) {
				status = CommandStatus.error;
			}
			stop_time = new Date().getTime();
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
			stop_time = new Date().getTime();
		}
		
		public boolean timeout(long now) {
			if (background || now - read_time < TIMEOUT) {
				return false;
			} else {
				if (thread != null) {
					LOG.info(String.format("command timeout: %s", thread.getName()));
					try {
						thread.interrupt();
					} catch (Exception e) {}
					thread = null;
				}
				
				buffer.clear();
				
				if (status == CommandStatus.running) {
					status = CommandStatus.timeout;
					stop_time = new Date().getTime();
				}
				return true;
			}
		}
	}
}
