package cn.batchfile.stat.util.cmd;

public class ProcessHook extends Thread {
	private final Process process;

	public ProcessHook(Process process) {
		super("CommandlineUtils process shutdown hook");
		this.process = process;
		this.setContextClassLoader(null);
	}

	public void run() {
		process.destroy();
	}
}
