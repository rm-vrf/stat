package cn.batchfile.stat.server.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

@org.springframework.stereotype.Service
public class DockerService {
	private static final Logger LOG = LoggerFactory.getLogger(DockerService.class);
	private Map<String, DockerClient> dockerClients = new ConcurrentHashMap<>();
	
	public DockerClient getDockerClient(String dockerHost, String apiVersion) {
		String key = dockerHost + "_" + apiVersion;
		if (!dockerClients.containsKey(key)) {
			synchronized (dockerClients) {
				if (!dockerClients.containsKey(key)) {
					LOG.info("create docker client, dockerHost: {}, apiVersion: {}", dockerHost, apiVersion);
					DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
							.withDockerHost("tcp://" + dockerHost).withApiVersion(apiVersion).build();
					DockerClient docker = DockerClientBuilder.getInstance(config).build();
					dockerClients.put(key, docker);
				}
			}
		}
		return dockerClients.get(key);
	}
	
	public DockerClient getDockerClient(String dockerHost) {
		String key = dockerHost;
		if (!dockerClients.containsKey(key)) {
			synchronized (dockerClients) {
				if (!dockerClients.containsKey(key)) {
					LOG.info("create docker client, dockerHost: {}", dockerHost);
					DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
							.withDockerHost("tcp://" + dockerHost).build();
					DockerClient docker = DockerClientBuilder.getInstance(config).build();
					dockerClients.put(key, docker);
				}
			}
		}
		return dockerClients.get(key);
	}
}
