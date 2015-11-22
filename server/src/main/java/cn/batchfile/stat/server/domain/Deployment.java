package cn.batchfile.stat.server.domain;

import com.alibaba.fastjson.annotation.JSONField;

public class Deployment {
	@JSONField(name="agent_id")
	private String agentId;
	private String name;
	private String description;
	@JSONField(name="instance_count")
	private int instanceCount;
	@JSONField(name="pid_file")
	private String pidFile;
	@JSONField(name="working_directory")
	private String workingDirectory;
	private String environment;
	@JSONField(name="start_command")
	private String startCommand;
	@JSONField(name="stop_command")
	private String stopCommand;
	@JSONField(name="java_home")
	private String javaHome;
	@JSONField(name="jre_home")
	private String jreHome;
	private String classpath;
	@JSONField(name="main_class")
	private String mainClass;
	private String jar;
	private String arguments;
	@JSONField(name="vm_arguments")
	private String vmArguments;

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getInstanceCount() {
		return instanceCount;
	}

	public void setInstanceCount(int instanceCount) {
		this.instanceCount = instanceCount;
	}

	public String getPidFile() {
		return pidFile;
	}

	public void setPidFile(String pidFile) {
		this.pidFile = pidFile;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getStartCommand() {
		return startCommand;
	}

	public void setStartCommand(String startCommand) {
		this.startCommand = startCommand;
	}

	public String getStopCommand() {
		return stopCommand;
	}

	public void setStopCommand(String stopCommand) {
		this.stopCommand = stopCommand;
	}

	public String getJavaHome() {
		return javaHome;
	}

	public void setJavaHome(String javaHome) {
		this.javaHome = javaHome;
	}

	public String getJreHome() {
		return jreHome;
	}

	public void setJreHome(String jreHome) {
		this.jreHome = jreHome;
	}

	public String getClasspath() {
		return classpath;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getJar() {
		return jar;
	}

	public void setJar(String jar) {
		this.jar = jar;
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public String getVmArguments() {
		return vmArguments;
	}

	public void setVmArguments(String vmArguments) {
		this.vmArguments = vmArguments;
	}
}
