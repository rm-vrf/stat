package cn.batchfile.stat.server.domain.node;

public class Containers {
	private Integer total;
	private Integer running;
	private Integer paused;
	private Integer stopped;

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getRunning() {
		return running;
	}

	public void setRunning(Integer running) {
		this.running = running;
	}

	public Integer getPaused() {
		return paused;
	}

	public void setPaused(Integer paused) {
		this.paused = paused;
	}

	public Integer getStopped() {
		return stopped;
	}

	public void setStopped(Integer stopped) {
		this.stopped = stopped;
	}
}
