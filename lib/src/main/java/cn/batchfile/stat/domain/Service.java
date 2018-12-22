package cn.batchfile.stat.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 服务
 * @author lane.cn@gmail.com
 *
 */
public class Service {
	
	private String name;
	private List<Artifact> artifacts;
	private List<String> runs;
	private List<Integer> ports;
	private String uid;
	private String workDirectory;
	private Integer stopSignal;
	private Integer stopGracePeriod;
	private String command;
	private Map<String, String> environment;
	private List<String> dependsOn;
	private HealthCheck healthCheck;
	private List<String> labels;
	private Deploy deploy;
	private Logging logging;

	/**
	 * 名称
	 * @return 名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 名称
	 * @param name 名称
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 部署物
	 * @return 部署物
	 */
	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	/**
	 * 部署物
	 * @param artifacts 部署物
	 */
	public void setArtifacts(List<Artifact> artifacts) {
		this.artifacts = artifacts;
	}

	/**
	 * 部署命令
	 * @return 部署命令
	 */
	public List<String> getRuns() {
		return runs;
	}

	/**
	 * 部署命令
	 * @param runs 部署命令
	 */
	public void setRuns(List<String> runs) {
		this.runs = runs;
	}

	/**
	 * 端口
	 * @return 端口
	 */
	public List<Integer> getPorts() {
		return ports;
	}

	/**
	 * 端口
	 * @param ports 端口
	 */
	public void setPorts(List<Integer> ports) {
		this.ports = ports;
	}

	/**
	 * 用户ID
	 * @return 用户ID
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * 用户ID
	 * @param uid 用户ID
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * 工作目录
	 * @return 工作目录
	 */
	public String getWorkDirectory() {
		return workDirectory;
	}

	/**
	 * 工作目录
	 * @param workDirectory 工作目录
	 */
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	/**
	 * 停止信号
	 * @return 停止信号
	 */
	public Integer getStopSignal() {
		return stopSignal;
	}

	/**
	 * 停止信号
	 * @param stopSignal 停止信号
	 */
	public void setStopSignal(Integer stopSignal) {
		this.stopSignal = stopSignal;
	}

	/**
	 * 停止时间
	 * @return 停止时间
	 */
	public Integer getStopGracePeriod() {
		return stopGracePeriod;
	}

	/**
	 * 停止时间
	 * @param stopGracePeriod 停止时间
	 */
	public void setStopGracePeriod(Integer stopGracePeriod) {
		this.stopGracePeriod = stopGracePeriod;
	}

	/**
	 * 服务命令
	 * @return 服务命令
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * 服务命令
	 * @param command 服务命令
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * 环境变量
	 * @return 环境变量
	 */
	public Map<String, String> getEnvironment() {
		return environment;
	}

	/**
	 * 环境变量
	 * @param environment 环境变量
	 */
	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}

	/**
	 * 依赖服务
	 * @return 依赖服务
	 */
	public List<String> getDependsOn() {
		return dependsOn;
	}

	/**
	 * 依赖服务
	 * @param dependsOn 依赖服务
	 */
	public void setDependsOn(List<String> dependsOn) {
		this.dependsOn = dependsOn;
	}

	/**
	 * 健康检查
	 * @return 健康检查
	 */
	public HealthCheck getHealthCheck() {
		return healthCheck;
	}

	/**
	 * 健康检查
	 * @param healthCheck 健康检查
	 */
	public void setHealthCheck(HealthCheck healthCheck) {
		this.healthCheck = healthCheck;
	}

	/**
	 * 标签
	 * @return 标签
	 */
	public List<String> getLabels() {
		return labels;
	}

	/**
	 * 标签
	 * @param labels 标签
	 */
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	/**
	 * 部署选项
	 * @return 部署选项
	 */
	public Deploy getDeploy() {
		return deploy;
	}

	/**
	 * 部署选项
	 * @param deploy 部署选项
	 */
	public void setDeploy(Deploy deploy) {
		this.deploy = deploy;
	}

	/**
	 * 日志
	 * @return 日志
	 */
	public Logging getLogging() {
		return logging;
	}

	/**
	 * 日志
	 * @param logging 日志
	 */
	public void setLogging(Logging logging) {
		this.logging = logging;
	}
	
}
