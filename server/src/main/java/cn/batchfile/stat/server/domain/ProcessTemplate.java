package cn.batchfile.stat.server.domain;

import com.alibaba.fastjson.annotation.JSONField;

public class ProcessTemplate {
	private String name;
	private String description;
	@JSONField(name="contains_every")
	private String containsEvery;
	@JSONField(name="contains_any")
	private String containsAny;
	@JSONField(name="contains_no")
	private String containsNo;
	@JSONField(name="running_instance")
	private int runningInstance;
	@JSONField(name="working_directory")
	private String workingDirectory;
	@JSONField(name="java_home")
	private String javaHome;
	@JSONField(name="jre_home")
	private String jreHome;
	@JSONField(name="main_class")
	private String mainClass;
	private String jar;
	private String arguments;
	@JSONField(name="vm_arguments")
	private String vmArguments;
	private String classpath;
	private String environment;
	@JSONField(name="start_command")
	private String startCommand;
	@JSONField(name="stop_command")
	private String stopCommand;
	
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
	
	public String getContainsEvery() {
		return containsEvery;
	}
	
	public void setContainsEvery(String containsEvery) {
		this.containsEvery = containsEvery;
	}
	
	public String getContainsAny() {
		return containsAny;
	}
	
	public void setContainsAny(String containsAny) {
		this.containsAny = containsAny;
	}
	
	public String getContainsNo() {
		return containsNo;
	}
	
	public void setContainsNo(String containsNo) {
		this.containsNo = containsNo;
	}
	
	public int getRunningInstance() {
		return runningInstance;
	}
	
	public void setRunningInstance(int runningInstance) {
		this.runningInstance = runningInstance;
	}
	
	public String getWorkingDirectory() {
		return workingDirectory;
	}
	
	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
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
	
	public String getClasspath() {
		return classpath;
	}
	
	public void setClasspath(String classpath) {
		this.classpath = classpath;
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
}
