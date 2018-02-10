package cn.batchfile.stat.server.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty3Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service
public class ElasticService {
	protected static final Logger log = LoggerFactory.getLogger(ElasticService.class);

	@Value("${elastic.port:51027}")
	private int elasticHttpPort;

	@Value("${elastic.port:51028}")
	private int elasticTcpPort;

	@Value("${store.directory}")
	private String storeDirectory;

	@Value("${mapping.resource.path}")
	public String mappingResourcePath;

	private Node node;

	public Node getNode() {
		return node;
	}

	@SuppressWarnings({ "resource", "unchecked" })
	@PostConstruct
	public void init() throws NodeValidationException, InterruptedException, ExecutionException, IOException {
		// 计算工作目录
		String dataPath = new File(new File(storeDirectory), "index").getAbsolutePath();

		// 启动elasticsearch
		log.info("start elastic, port: {}, path: {}", elasticHttpPort, dataPath);

		// 构建设置
		Builder builder = Settings.builder();
		builder.put("cluster.name", clusterName()).put("node.name", "node-1").put("node.master", String.valueOf(true))
				.put("node.data", String.valueOf(true)).put("transport.type", "local")
				.put("http.enabled", String.valueOf(elasticHttpPort > 0)).put("http.cors.enabled", String.valueOf(true))
				.put("http.cors.allow-origin", "*").put("http.type", "netty3").put("network.bind_host", "0.0.0.0")
				.put("http.port", String.valueOf(elasticHttpPort))
				.put("transport.tcp.port", String.valueOf(elasticTcpPort)).put("path.home", dataPath);

		// 得到设置对象
		Settings settings = builder.build();

		@SuppressWarnings("rawtypes")
		Collection plugins = Arrays.asList(Netty3Plugin.class, Netty4Plugin.class);
		node = new RunnerNode(settings, plugins).start();

		// 启动es节点
		node.start();

		// 放入索引模板
		putIndexTemplate();
		log.info("elastic node started, {}", node.toString());
	}
	
	private void putIndexTemplate() throws InterruptedException, ExecutionException, IOException {
		// 判断是否设置了映射路径
		if (StringUtils.isEmpty(mappingResourcePath)) {
			log.info("empty mapping resource path, exit template");
			return;
		}

		// 寻找mapping文件资源
		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resourcePatternResolver.getResources(mappingResourcePath);

		for (Resource resource : resources) {
			log.info("find mapping file: {}, [{}]", resource.toString(), resource.getClass().getName());
			String path = getPathOfResource(resource);

			// 从文件名称上得到索引名称和类型名称
			File file = new File(path);
			String indexName = file.getParentFile().getName();
			String typeName = StringUtils.substringBeforeLast(file.getName(), ".");

			// 读文件内容
			String content = getContentOfResource(resource);
			log.info("get template: {}/{}, length: {}", indexName, typeName, StringUtils.length(content));
			JSONObject json = JSONObject.parseObject(content);

			// 添加类型映射
			String templateName = String.format("template-%s-%s", indexName, typeName);
			PutIndexTemplateResponse response = node.client().admin().indices().preparePutTemplate(templateName)
					.setTemplate(indexName + "*").addMapping(typeName, json).execute().get();
			log.info("template response: {}", response.toString());
			log.info("add template: {}", templateName);
		}
	}

	private String getContentOfResource(Resource resource) throws IOException {
		if (resource instanceof ClassPathResource) {
			String path = ((ClassPathResource) resource).getPath();
			InputStream stream = getClass().getClassLoader().getResource(path).openStream();
			try {
				List<String> lines = IOUtils.readLines(stream);
				return StringUtils.join(lines, StringUtils.EMPTY);
			} finally {
				IOUtils.closeQuietly(stream);
			}
		} else if (resource instanceof FileSystemResource) {
			String path = ((FileSystemResource) resource).getPath();
			File file = new File(path);
			return FileUtils.readFileToString(file, Charset.forName("utf-8"));
		} else {
			throw new RuntimeException("error when get resouece content");
		}
	}

	private String getPathOfResource(Resource resource) {
		if (resource instanceof ClassPathResource) {
			return ((ClassPathResource) resource).getPath();
		} else if (resource instanceof FileSystemResource) {
			return ((FileSystemResource) resource).getPath();
		} else {
			throw new RuntimeException("error when get resouece path");
		}
	}

	private String clusterName() throws UnknownHostException {
		InetAddress ip = InetAddress.getLocalHost();
		String clusterName = ip.getHostName();
		clusterName = StringUtils.replaceEach(clusterName, new String[] { ":", ".", "-" },
				new String[] { "_", "_", "_" });
		return clusterName;
	}

	class RunnerNode extends Node {
		private final Collection<Class<? extends Plugin>> plugins;

		public RunnerNode(final Environment tmpEnv, final Collection<Class<? extends Plugin>> classpathPlugins) {
			super(tmpEnv, classpathPlugins);
			this.plugins = classpathPlugins;
		}

		public RunnerNode(final Settings preparedSettings, final Collection<Class<? extends Plugin>> classpathPlugins) {
			this(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
		}

		public Collection<Class<? extends Plugin>> getPlugins() {
			return plugins;
		}

	}
}
