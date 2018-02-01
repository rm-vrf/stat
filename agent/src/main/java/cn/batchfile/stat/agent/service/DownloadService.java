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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.agent.types.App;
import cn.batchfile.stat.util.Lock;

@Service
public class DownloadService {
	protected static final Logger log = LoggerFactory.getLogger(DownloadService.class);
	
	public void downloadArtifacts(App app) throws IOException, InterruptedException {
		File dir = new File(app.getWorkingDirectory());
		if (!dir.exists()) {
			FileUtils.forceMkdir(dir);
		}
		log.info("working directory: {}", dir);
		
		if (app.getUris() != null) {
			for (String uri : app.getUris()) {
				downloadArtifact(dir, uri);
			}
		}
	}

	private void downloadArtifact(File dir, String uri) throws ClientProtocolException, IOException, InterruptedException {
		log.info("check artifact, uri: " + uri);
		
		//检查下载地址
		if (StringUtils.isEmpty(uri)) {
			return;
		}
		
		//从下载地址上得到文件名
		String fileBaseName = getBaseName(uri);
		
		//得到文件的最近修改时间
		String remoteLastModified = getRemoteLastModified(uri);
		log.info("remote file timestamp: " + remoteLastModified);
		if (StringUtils.isEmpty(remoteLastModified)) {
			//如果得不到远程时间戳，不必下载文件了，直接使用本地的包，没有就不能运行
			return;
		}
		
		//锁定文件
		Lock lock = null;
		try {
			lock = createLock(dir, fileBaseName);
			
			//得到本地的最近修改时间
			String localLastModified = getLocalLastModified(dir, fileBaseName);
			log.info("local file timestamp: " + localLastModified);
			//如果时间不相同，下载最新的包
			if (!StringUtils.equals(remoteLastModified, localLastModified)) {
				log.info("downloading...");
				File pkg = download(uri, dir, fileBaseName, remoteLastModified);
				log.info("downloaded");
				
				//解压到根目录
				if (StringUtils.endsWithIgnoreCase(pkg.getName(), ".zip")) {
					unzip(dir, pkg);
					log.info("unzip ok");
				}
			}
		} finally {
			try {
				lock.getFileLock().release();
			} catch (Exception e) {}
			IOUtils.closeQuietly(lock.getRandomAccessFile());
		}
	}

	private File download(String uri, File dir, String fileBaseName, String lastModified) throws IOException {
		
		//目标文件地址
		File distFile = new File(dir, fileBaseName);
		File timestampFile = new File(dir, String.format("%s.%s", fileBaseName, "lastModified"));
		
		//删除当前的文件
		FileUtils.deleteQuietly(distFile);
		FileUtils.deleteQuietly(timestampFile);
		
		//创建目标文件
		distFile.createNewFile();
		timestampFile.createNewFile();
		
		//下载文件流，向目标文件写入
		OutputStream out = null;
		InputStream in = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;
		try {
			//创建文件写入流
			out = new FileOutputStream(distFile);
			
			//构建请求对象
			RequestConfig config = RequestConfig.custom().setConnectTimeout(20000).build();
			
			//执行请求
			httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			HttpGet req = new HttpGet(uri);
			resp = httpClient.execute(req);

			//检查返回代码
			int code = resp.getStatusLine().getStatusCode();
			if (code >= 200 && code < 300) {
				in = resp.getEntity().getContent();
				IOUtils.copyLarge(in, out);
			} else {
				try {req.abort();} catch (Exception e) {}
				throw new RuntimeException("error when get file: " + uri + ", code: " + code);
			}
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(resp);
			IOUtils.closeQuietly(httpClient);
		}
		
		//写入时间戳文件
		FileUtils.writeByteArrayToFile(timestampFile, lastModified.getBytes());
		
		return distFile;
	}
	
	private String getLocalLastModified(File dir, String fileBaseName) throws IOException {
		String lastModified = StringUtils.EMPTY;
		
		//构建时间戳文件的名称
		String fileName = String.format("%s.%s", fileBaseName, "lastModified");
		File file = new File(dir, fileName);
		
		//检查文件是否存在
		if (!file.exists()) {
			return lastModified;
		}
		
		//读文件内容
		String s = FileUtils.readFileToString(file);
		if (StringUtils.isNotEmpty(s)) {
			lastModified = s;
		}
		
		return lastModified;
	}
	
	private Lock createLock(File dir, String fileBaseName) throws IOException, InterruptedException {
		//创建锁文件
		File lockFile = new File(dir, String.format("%s.%s", fileBaseName, "lock"));
		if (!lockFile.exists()) {
			lockFile.createNewFile();
		}
		
		//对lock文件加锁
		RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
		FileChannel fc = raf.getChannel();
		FileLock fl = fc.lock();
		
		//返回锁对象
		return new Lock(raf, fl);
	}
	
	private String getBaseName(String uri) {
		String fileBaseName = StringUtils.substringAfterLast(uri, "/");
		if (StringUtils.contains(fileBaseName, "?")) {
			fileBaseName = StringUtils.substringBefore(fileBaseName, "?");
		}
		return fileBaseName;
	}

	private String getRemoteLastModified(String uri) throws ClientProtocolException, IOException {
		String lastModified = StringUtils.EMPTY;
		
		//构建请求对象
		RequestConfig config = RequestConfig.custom().setConnectTimeout(20000).build();
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;
		
		try {
			//执行请求
			httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			HttpHead req = new HttpHead(uri);
			resp = httpClient.execute(req);
			
			//检查返回代码
			int code = resp.getStatusLine().getStatusCode();
			if (code >= 200 && code < 300) {
				String value = getHeaderValue(resp, new String[]{"Last-Modified", "ETag"});
				if (StringUtils.isNotBlank(value)) {
					lastModified = value;
				}
			} else {
				try {req.abort();} catch (Exception e) {}
				throw new RuntimeException("error when head file: " + uri + ", code: " + code);
			}
		} catch (Exception e) {
			log.error("error when send head", e);
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
		return null;
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
					log.debug(" creating: {}", name);
					FileUtils.forceMkdir(f);
				} else {
					log.debug("inflating: {}", name);
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
