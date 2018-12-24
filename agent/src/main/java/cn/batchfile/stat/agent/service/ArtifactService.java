package cn.batchfile.stat.agent.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.batchfile.stat.agent.util.Lock;
import cn.batchfile.stat.agent.util.cmd.CommandLineCallable;
import cn.batchfile.stat.agent.util.cmd.CommandLineExecutor;
import cn.batchfile.stat.domain.Artifact;
import cn.batchfile.stat.domain.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@org.springframework.stereotype.Service
public class ArtifactService {

	protected static final Logger LOG = LoggerFactory.getLogger(ArtifactService.class);
	private Counter downloadCounter;
	private Counter downloadSize;
	private Timer downloadResponseTime;
	
	public ArtifactService(MeterRegistry registry) {
		downloadCounter = Counter.builder("artifact.download.file").register(registry);
		downloadSize = Counter.builder("artifact.download.size").baseUnit("bytes").register(registry);
		downloadResponseTime = Timer.builder("artifact.download.response.time").register(registry);
	}

	/**
	 * 下载
	 * 
	 * @param service
	 *            服务
	 * @throws IOException
	 *             异常
	 * @throws InterruptedException
	 *             异常
	 */
	public void downloadArtifacts(Service service) throws IOException, InterruptedException {
		List<Artifact> artifacts = service.getArtifacts();
		String wd = service.getWorkDirectory();

		// 下载部署物
		if (artifacts != null) {
			for (Artifact artifact : artifacts) {
				downloadArtifact(wd, artifact);
			}
		}
		
		// 运行脚本
		if (service.getRuns() != null) {
			for (String cmd : service.getRuns()) {
				int ret = 0;
				try {
					ret = execute(cmd, wd);
				} catch (Exception e) {
					ret = -1;
					LOG.error("error when run command: " + cmd, e);
				}
				LOG.info("RUN: {}, RET: {}", cmd, ret);
			}
		}
	}

	private int execute(String cmd, String workDirectory) throws Exception {
		Commandline command = new Commandline();
		command.setWorkingDirectory(new File(workDirectory).getAbsolutePath());
		
		String[] ary = CommandLineUtils.translateCommandline(cmd);
		for (String s : ary) {
			Arg argObject = command.createArg();
			argObject.setValue(s);
		}
		CommandLineCallable callable = CommandLineExecutor.executeCommandLine(command, null, null, null, 0);
		return callable.call();
	}

	private void downloadArtifact(String workDirectory, Artifact artifact) throws IOException, InterruptedException {
		LOG.info("download artifact, uri: {}", artifact.getUri());

		// 创建目录
		File path = new File(StringUtils.isEmpty(artifact.getPath()) ? workDirectory : artifact.getPath());
		if (!path.exists()) {
			FileUtils.forceMkdir(path);
			LOG.info("mkdir {}", path.getAbsolutePath());
		}

		// 检查地址
		if (StringUtils.isEmpty(artifact.getUri())) {
			return;
		}

		// 从下载地址上得到文件名
		String baseName = getBaseName(artifact.getUri());

		// 得到文件的最近修改时间
		String remoteLastModified = getRemoteLastModified(artifact);
		LOG.info("remote file timestamp: {}", remoteLastModified);

		// 锁定文件
		Lock lock = null;
		try {
			lock = createLock(path, baseName);

			// 得到本地的最近修改时间
			String localLastModified = getLocalLastModified(path, baseName);
			LOG.info("local file timestamp: {}", localLastModified);

			// 如果时间不相同，下载最新的包
			if (!StringUtils.equals(remoteLastModified, localLastModified)) {
				LOG.info("downloading ...");
				long begin = System.currentTimeMillis();
				File pkg = download(artifact, path, baseName, remoteLastModified);
				LOG.info("downloaded file size: {}", FileUtils.byteCountToDisplaySize(pkg.length()));
				downloadResponseTime.record(System.currentTimeMillis() - begin, TimeUnit.MICROSECONDS);
				downloadCounter.increment();
				downloadSize.increment(pkg.length());

				// 解压到根目录
				if (StringUtils.endsWithIgnoreCase(pkg.getName(), ".zip")) {
					unzip(path, pkg);
					LOG.info("unzip ok");
				}
			}
		} finally {
			try {
				lock.getFileLock().release();
			} catch (Exception e) {
			}
			IOUtils.closeQuietly(lock.getRandomAccessFile());
		}
	}

	private File download(Artifact artifact, File path, String baseName, String lastModified) throws IOException {
		// 目标文件地址
		File distFile = new File(path, baseName);
		File timestampFile = new File(path, String.format("%s.%s", baseName, "lastModified"));

		// 删除当前的文件
		FileUtils.deleteQuietly(distFile);
		FileUtils.deleteQuietly(timestampFile);

		// 创建目标文件
		distFile.createNewFile();
		timestampFile.createNewFile();

		// 下载文件流，向目标文件写入
		OutputStream out = null;
		InputStream in = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;
		try {
			// 创建文件写入流
			out = new FileOutputStream(distFile);

			// 构建请求对象
			RequestConfig config = RequestConfig.custom().setConnectTimeout(20000).build();

			// 执行请求
			httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			
			// TODO HTTP 请求要加上权限认证
			HttpGet req = new HttpGet(artifact.getUri());
			resp = httpClient.execute(req);

			// 检查返回代码
			int code = resp.getStatusLine().getStatusCode();
			if (code >= 200 && code < 300) {
				in = resp.getEntity().getContent();
				IOUtils.copyLarge(in, out);
			} else {
				try {
					req.abort();
				} catch (Exception e) {
				}
				throw new RuntimeException("error when get file: " + artifact.getUri() + ", code: " + code);
			}
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(resp);
			IOUtils.closeQuietly(httpClient);
		}

		// 写入时间戳文件
		FileUtils.writeByteArrayToFile(timestampFile, lastModified.getBytes());

		return distFile;
	}

	private String getLocalLastModified(File path, String baseName) throws IOException {
		String lastModified = StringUtils.EMPTY;

		// 构建时间戳文件的名称
		String fileName = String.format("%s.%s", baseName, "lastModified");
		File file = new File(path, fileName);

		// 检查文件是否存在
		if (!file.exists()) {
			return lastModified;
		}

		// 读文件内容
		String s = FileUtils.readFileToString(file);
		if (StringUtils.isNotEmpty(s)) {
			lastModified = s;
		}

		return lastModified;
	}

	private Lock createLock(File dir, String baseName) throws IOException, InterruptedException {
		// 创建锁文件
		File lockFile = new File(dir, String.format("%s.%s", baseName, "lock"));
		if (!lockFile.exists()) {
			lockFile.createNewFile();
		}

		// 对lock文件加锁
		RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
		FileChannel fc = raf.getChannel();
		FileLock fl = fc.lock();

		// 返回锁对象
		return new Lock(raf, fl);
	}

	private String getBaseName(String uri) {
		String fileBaseName = StringUtils.substringAfterLast(uri, "/");
		if (StringUtils.contains(fileBaseName, "?")) {
			fileBaseName = StringUtils.substringBefore(fileBaseName, "?");
		}
		return fileBaseName;
	}

	private String getRemoteLastModified(Artifact artifact) throws IOException {
		String lastModified = StringUtils.EMPTY;

		// 构建请求对象
		RequestConfig config = RequestConfig.custom().setConnectTimeout(20000).build();
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;

		try {
			// 执行请求
			httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			// TODO HTTP 请求要加上权限认证
			
			HttpHead req = new HttpHead(artifact.getUri());
			resp = httpClient.execute(req);

			// 检查返回代码
			int code = resp.getStatusLine().getStatusCode();
			if (code >= 200 && code < 300) {
				String value = getHeaderValue(resp, new String[] { "Last-Modified", "ETag" });
				if (StringUtils.isNotBlank(value)) {
					lastModified = value;
				}
			} else {
				try {
					req.abort();
				} catch (Exception e) {
				}
				throw new RuntimeException("error when head file: " + artifact.getUri() + ", code: " + code);
			}
		} catch (Exception e) {
			LOG.error("error when send head", e);
		} finally {
			IOUtils.closeQuietly(resp);
			IOUtils.closeQuietly(httpClient);
		}
		return lastModified;
	}

	private String getHeaderValue(CloseableHttpResponse resp, String[] headerNames) {
		for (String name : headerNames) {
			Header[] headers = resp.getHeaders(name);
			if (headers != null) {
				for (Header header : headers) {
					if (StringUtils.isNotEmpty(header.getValue())) {
						return header.getValue();
					}
				}
			}
		}
		return StringUtils.EMPTY;
	}

	private void unzip(File dir, File zipFile) throws IOException {
		InputStream inputStream = null;
		ZipInputStream zis = null;
		try {
			inputStream = new FileInputStream(zipFile);
			zis = new ZipInputStream(inputStream);
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
				File f = new File(dir, name);
				if (f.exists()) {
					FileUtils.forceDelete(f);
				}
				if (StringUtils.endsWith(name, "/")) {
					LOG.debug(" creating: {}", name);
					FileUtils.forceMkdir(f);
				} else {
					LOG.debug("inflating: {}", name);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[2048];
					int num;
					while ((num = zis.read(buf, 0, 2048)) != -1) {
						out.write(buf, 0, num);
					}
					FileUtils.writeByteArrayToFile(f, out.toByteArray());
				}
			}
		} finally {
			IOUtils.closeQuietly(zis);
			IOUtils.closeQuietly(inputStream);
		}
	}
}
