package cn.batchfile.stat.server.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.TopContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig.Builder;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;

@org.springframework.stereotype.Service
public class DockerService {
	private static final int READ_TIMEOUT = 1800000;
	public static final int CONNECT_TIMEOUT = 2000;
	private static final int MAX_PER_ROUTE_CONNECTIONS = 10;
	private static final Logger LOG = LoggerFactory.getLogger(DockerService.class);
	private Map<String, DockerClient> dockerClients = new ConcurrentHashMap<>();
	
	public int getConnectTimeoutSeconds() {
		return CONNECT_TIMEOUT / 1000;
	}
	
	public Version getVersion(String dockerHost) {
		DockerClient docker = getDockerClient(dockerHost);
		return docker.versionCmd().exec();
	}
	
	public Info getInfo(String dockerHost, String apiVersion) {
		DockerClient docker = getDockerClient(dockerHost, apiVersion);
		return docker.infoCmd().exec();
	}
	
	public List<Container> listContainers(String dockerHost, String apiVersion, boolean showAll) {
		DockerClient docker = getDockerClient(dockerHost, apiVersion);
		List<Container> containers = docker.listContainersCmd().withShowAll(showAll).exec();
		return containers;
	}
	
	public InspectContainerResponse inspectContainer(String dockerHost, String apiVersion, String containerId) {
		DockerClient docker = getDockerClient(dockerHost, apiVersion);
		return docker.inspectContainerCmd(containerId).exec();
	}
	
	public TopContainerResponse topContainer(String dockerHost, String apiVersion, String containerId) {
		DockerClient docker = getDockerClient(dockerHost, apiVersion);
		return docker.topContainerCmd(containerId).exec();
	}
	
	public void startContainer(String dockerHost, String apiVersion, String containerId) {
		DockerClient docker = getDockerClient(dockerHost, apiVersion);
		docker.startContainerCmd(containerId).exec();
	}
	
	public void stopContainer(String dockerHost, String apiVersion, String containerId) {
		DockerClient docker = getDockerClient(dockerHost, apiVersion);
		docker.stopContainerCmd(containerId).exec();
	}

	public void removeContainer(String dockerHost, String apiVersion, String containerId) {
		DockerClient docker = getDockerClient(dockerHost, apiVersion);
		docker.removeContainerCmd(containerId).exec();
	}
	
	public ExecCreateCmdResponse createExec(String dockerHost, String apiVersion, 
			String containerId, String... cmd) {
		
		DockerClient docker = getDockerClient(dockerHost, apiVersion);
		ExecCreateCmdResponse resp = docker.execCreateCmd(containerId)
			.withAttachStdin(true)
			.withAttachStdout(true)
			.withAttachStderr(true)
			.withTty(true)
			.withCmd(cmd)
			.exec();
		return resp;
	}

	private DockerClient getDockerClient(String dockerHost, String apiVersion) {
		String key = dockerHost + "_" + apiVersion;
		if (!dockerClients.containsKey(key)) {
			synchronized (dockerClients) {
				if (!dockerClients.containsKey(key)) {
					LOG.info("create docker client, dockerHost: {}, apiVersion: {}", dockerHost, apiVersion);
					
					Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
							.withDockerHost("tcp://" + dockerHost);
					if (StringUtils.isNotEmpty(apiVersion)) {
						builder.withApiVersion(apiVersion);
					}
					DockerClientConfig config = builder.build();
					
					@SuppressWarnings("resource")
					DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
							.withConnectTimeout(CONNECT_TIMEOUT)
							.withReadTimeout(READ_TIMEOUT)
							.withMaxPerRouteConnections(MAX_PER_ROUTE_CONNECTIONS);
					
					DockerClient docker = DockerClientBuilder.getInstance(config)
							.withDockerCmdExecFactory(dockerCmdExecFactory)
							.build();
					
					dockerClients.put(key, docker);
				}
			}
		}
		return dockerClients.get(key);
	}
	
	private DockerClient getDockerClient(String dockerHost) {
		return getDockerClient(dockerHost, null);
	}
}
