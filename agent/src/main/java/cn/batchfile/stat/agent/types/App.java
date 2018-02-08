package cn.batchfile.stat.agent.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
	
	private String name;
	private String toProcess;
	private String workingDirectory;
	private Map<String, String> envs = new HashMap<String, String>();
	private List<String> args = new ArrayList<String>();
	private List<Integer> ports = new ArrayList<Integer>();
	private List<String> uris = new ArrayList<String>();
	private int killSignal = 15;
	private List<HealthCheck> healthChecks;
	private boolean start;
	private ResAlloc resAlloc;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getToProcess() {
		return toProcess;
	}

	public void setToProcess(String toProcess) {
		this.toProcess = toProcess;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public Map<String, String> getEnvs() {
		return envs;
	}

	public void setEnvs(Map<String, String> envs) {
		this.envs = envs;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	public List<Integer> getPorts() {
		return ports;
	}

	public void setPorts(List<Integer> ports) {
		this.ports = ports;
	}

	public List<String> getUris() {
		return uris;
	}

	public void setUris(List<String> uris) {
		this.uris = uris;
	}

	public int getKillSignal() {
		return killSignal;
	}

	public void setKillSignal(int killSignal) {
		this.killSignal = killSignal;
	}

	public List<HealthCheck> getHealthChecks() {
		return healthChecks;
	}

	public void setHealthChecks(List<HealthCheck> healthChecks) {
		this.healthChecks = healthChecks;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public ResAlloc getResAlloc() {
		return resAlloc;
	}

	public void setResAlloc(ResAlloc resAlloc) {
		this.resAlloc = resAlloc;
	}
}
