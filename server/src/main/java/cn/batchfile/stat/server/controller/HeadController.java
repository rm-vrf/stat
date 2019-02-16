package cn.batchfile.stat.server.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.batchfile.stat.server.service.MimeTypes;

@Controller
public class HeadController {
	private static final String SCRIPT = "var base_uri = location.href.replace(/_plugin\\/.*/, '');";
	private static final Map<String, String> DIRECTORIES = new HashMap<String, String>();
	private static final String INDEX = "index.html";
	private Map<String, byte[]> contents = new ConcurrentHashMap<>();
	private MimeTypes mimeTypes;

	@Value("${document.root:}")
	private String documentRoot;

	@Value("${elasticsearch.http.port}")
	private int httpPort;

	static {
		DIRECTORIES.put("/_plugin/head", "");
		DIRECTORIES.put("/_plugin/head/", "");
		DIRECTORIES.put("/admin", "");
		DIRECTORIES.put("/admin/", "");
	};

	@PostConstruct
	private void init() {
		mimeTypes = new MimeTypes();
	}

	@RequestMapping(path = { "/_plugin/**", "/admin/**" })
	public void plugin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getRequestURI();
		if (DIRECTORIES.containsKey(path)) {
			if (StringUtils.endsWith(path, "/")) {
				path += INDEX;
			} else {
				response.sendRedirect(path + "/");
				return;
			}
		}

		byte[] bytes = getContent(request, path);
		if (bytes == null || bytes.length == 0) {
			response.sendError(HttpStatus.NOT_FOUND.value());
			return;
		}

		String mimeType = mimeTypes.getMimeByExtension(path);
		if (StringUtils.equals("text/html", mimeType)) {
			mimeType += "; charset=UTF-8";
		}
		response.addHeader("Content-Type", mimeType);
		response.getOutputStream().write(bytes);
	}

	private byte[] getContent(HttpServletRequest request, String path) throws IOException {
		byte[] bytes = null;
		if (StringUtils.isEmpty(documentRoot)) {
			bytes = getClasspathContent(request, path);
		} else {
			bytes = getFileContent(request, path);
		}
		return bytes;
	}
	
	private byte[] getFileContent(HttpServletRequest request, String path) throws IOException {
		File file = new File(documentRoot + path);
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		byte[] buff = FileUtils.readFileToByteArray(file);
		buff = replaceContent(request, path, buff);
		return buff;
	}
	
	private byte[] getClasspathContent(HttpServletRequest request, String path) throws IOException {
		if (!contents.containsKey(path)) {
			InputStream stream = null;
			Reader reader = null;
			try {
				stream = getClass().getClassLoader().getResourceAsStream("META-INF/resources" + path);
				if (stream != null) {
					byte[] buff = new byte[stream.available()];
					IOUtils.readFully(stream, buff, 0, stream.available());
					buff = replaceContent(request, path, buff);
					contents.put(path, buff);
				} else {
					contents.put(path, new byte[] {});
				}
			} finally {
				try {
					reader.close();
				} catch (Exception e) {
				}
				try {
					stream.close();
				} catch (Exception e) {
				}
			}
		}
		return contents.get(path);
	}

	private byte[] replaceContent(HttpServletRequest request, String path, byte[] bytes)
			throws MalformedURLException, UnsupportedEncodingException {
		if (StringUtils.equals(path, "/_plugin/head/index.html")) {
			URL url = new URL(request.getRequestURL().toString());
			String protocol = url.getProtocol();
			String host = url.getHost();
			String replacement = String.format("var base_uri = \"%s://%s:%s/\";", protocol, host, httpPort);

			String s = new String(bytes);
			s = StringUtils.replace(s, SCRIPT, replacement);

			bytes = s.getBytes("UTF-8");
		}
		return bytes;
	}

}
