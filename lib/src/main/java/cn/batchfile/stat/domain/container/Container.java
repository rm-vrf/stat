package cn.batchfile.stat.domain.container;

import java.util.Date;
import java.util.List;

/**
 * 容器
 * @author Administrator
 *
 */
public class Container {

	private String id;
	private String node;
	private String namespace;
	private String service;
	private String name;
	private String image;
	private String command;
	private Date createTime;
	private List<Port> ports;
	private String state;
	private Date stateChangeTime;
	private String description;

	/**
	 * The ID of this container
	 * @return The ID of this container
	 */
	public String getId() {
		return id;
	}

	/**
	 * The ID of this container
	 * @param id The ID of this container
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 节点
	 * @return 节点
	 */
	public String getNode() {
		return node;
	}

	/**
	 * 节点
	 * @param node 节点
	 */
	public void setNode(String node) {
		this.node = node;
	}

	/**
	 * 命名空间
	 * @return 命名空间
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * 命名空间
	 * @param namespace 命名空间
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * 服务
	 * @return 服务
	 */
	public String getService() {
		return service;
	}

	/**
	 * 服务
	 * @param service 服务
	 */
	public void setService(String service) {
		this.service = service;
	}

	/**
	 * The names that this container has been given
	 * @return The names that this container has been given
	 */
	public String getName() {
		return name;
	}

	/**
	 * The names that this container has been given
	 * @param name The names that this container has been given
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The name of the image used when creating this container
	 * @return The name of the image used when creating this container
	 */
	public String getImage() {
		return image;
	}

	/**
	 * The name of the image used when creating this container
	 * @param image The name of the image used when creating this container
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * Command to run when starting the container
	 * @return Command to run when starting the container
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Command to run when starting the container
	 * @param command Command to run when starting the container
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * When the container was created
	 * @return When the container was created
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * When the container was created
	 * @param createTime When the container was created
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * The ports exposed by this container
	 * @return The ports exposed by this container
	 */
	public List<Port> getPorts() {
		return ports;
	}

	/**
	 * The ports exposed by this container
	 * @param ports The ports exposed by this container
	 */
	public void setPorts(List<Port> ports) {
		this.ports = ports;
	}

	/**
	 * The state of this container (e.g. Exited)
	 * @return The state of this container (e.g. Exited)
	 */
	public String getState() {
		return state;
	}

	/**
	 * The state of this container (e.g. Exited)
	 * @param state The state of this container (e.g. Exited)
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * 状态变化时间
	 * @return 状态变化时间
	 */
	public Date getStateChangeTime() {
		return stateChangeTime;
	}

	/**
	 * 状态变化时间
	 * @param stateChangeTime 状态变化时间
	 */
	public void setStateChangeTime(Date stateChangeTime) {
		this.stateChangeTime = stateChangeTime;
	}

	/**
	 * Additional human-readable status of this container (e.g. Exit 0)
	 * @return Additional human-readable status of this container (e.g. Exit 0)
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Additional human-readable status of this container (e.g. Exit 0)
	 * @param description Additional human-readable status of this container (e.g. Exit 0)
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
