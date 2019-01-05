package cn.batchfile.stat.server.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service
public class ElasticService {
	protected static final Logger log = LoggerFactory.getLogger(ElasticService.class);
	private static final String INDEX_DATE_FORMAT = "yyyy-MM-dd";

	@Value("${spring.data.elasticsearch.cluster-name}")
	public String clusterName;

	@Value("${elasticsearch.http.port}")
	public int httpPort;

	@Value("${elasticsearch.tcp.port}")
	private int tcpPort;

	@Value("${store.directory}")
	private String storeDirectory;

	@Value("${mapping.resource.path}")
	public String mappingResourcePath;

	private Node node;

	public Client getNode() {
		return node.client();
	}

	@SuppressWarnings({ "resource", "unchecked" })
	@PostConstruct
	public void init() throws NodeValidationException, InterruptedException, ExecutionException, IOException {
		// 计算工作目录
		String dataPath = new File(new File(storeDirectory), "index").getAbsolutePath();

		// 启动elasticsearch
		log.info("start elastic, tcp port: {}, http port: {}, path: {}", tcpPort, httpPort, dataPath);

		// 构建设置
		Builder builder = Settings.builder();
		builder.put("cluster.name", clusterName)
				.put("node.master", String.valueOf(true))
				.put("node.data", String.valueOf(true))
				.put("transport.type", "local")
				.put("http.enabled", String.valueOf(true))
				.put("http.cors.enabled", String.valueOf(true))
				.put("http.cors.allow-origin", "*")
				.put("http.type", "netty4")
				.put("network.bind_host", "0.0.0.0")
				.put("http.port", String.valueOf(httpPort))
				.put("transport.tcp.port", String.valueOf(tcpPort))
				.put("path.home", dataPath);

		// 得到设置对象
		Settings settings = builder.build();

		// 启动es节点
		@SuppressWarnings("rawtypes")
		Collection plugins = Arrays.asList(Netty4Plugin.class);
		node = new RunnerNode(settings, plugins).start();

		// 放入索引模板
		putIndexTemplate();
		log.info("elastic node started, {}", node.toString());
	}

	@Scheduled(initialDelay = 600000, fixedDelay = 600000)
	public void deleteHistory() {
		SimpleDateFormat format = new SimpleDateFormat(INDEX_DATE_FORMAT);
		Date now = new Date();

		GetIndexResponse response = node.client().admin().indices().prepareGetIndex().get();
		String[] indexNames = response.indices();
		if (indexNames != null) {
			for (String indexName : indexNames) {
				log.debug("index name: {}", indexName);
				try {
					String s = StringUtils.right(indexName, INDEX_DATE_FORMAT.length());
					Date date = format.parse(s);
					if (now.getTime() - date.getTime() > 7L * 86400000L) {
						node.client().admin().indices().prepareDelete(indexName).execute();
					}
				} catch (Exception e) {
					// pass
				}
			}
		}
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
			// log.info("find mapping file: {}, [{}]", resource.toString(),
			// resource.getClass().getName());
			String path = getPathOfResource(resource);

			// 从文件名称上得到索引名称和类型名称
			File file = new File(path);
			String indexName = file.getParentFile().getName();
			String typeName = StringUtils.substringBeforeLast(file.getName(), ".");

			// 读文件内容
			String content = getContentOfResource(resource);
			// log.info("get template: {}/{}, length: {}", indexName, typeName,
			// StringUtils.length(content));
			JSONObject json = JSONObject.parseObject(content);

			// 添加类型映射
			String templateName = String.format("template-%s-%s", indexName, typeName);
			PutIndexTemplateResponse response = node.client().admin().indices().preparePutTemplate(templateName)
					.setTemplate(indexName + "*").addMapping(typeName, json).execute().get();
			log.info("template response: {}", response.toString());
			// log.info("add template: {}", templateName);
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
			return FileUtils.readFileToString(file, "utf-8");
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
