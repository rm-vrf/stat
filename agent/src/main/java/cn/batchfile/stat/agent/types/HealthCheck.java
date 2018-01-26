package cn.batchfile.stat.agent.types;

public class HealthCheck {
	private int startupSeconds;
	private int intervalSeconds;
	private int maxConsecutiveFailures;
	private String path;
	private int portIndex;
	private String protocol;
	private int timeoutSeconds;

	public int getStartupSeconds() {
		return startupSeconds;
	}

	public void setStartupSeconds(int startupSeconds) {
		this.startupSeconds = startupSeconds;
	}

	public int getIntervalSeconds() {
		return intervalSeconds;
	}

	public void setIntervalSeconds(int intervalSeconds) {
		this.intervalSeconds = intervalSeconds;
	}

	public int getMaxConsecutiveFailures() {
		return maxConsecutiveFailures;
	}

	public void setMaxConsecutiveFailures(int maxConsecutiveFailures) {
		this.maxConsecutiveFailures = maxConsecutiveFailures;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getPortIndex() {
		return portIndex;
	}

	public void setPortIndex(int portIndex) {
		this.portIndex = portIndex;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}
}
