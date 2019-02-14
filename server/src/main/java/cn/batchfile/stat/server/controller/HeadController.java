package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HeadController {
	private static final String SCRIPT = "var base_uri = location.href.replace(/_plugin\\/.*/, '');";
	private Map<String, byte[]> contents = new ConcurrentHashMap<>(); 
	
	@Value("${elasticsearch.http.port}")
	public int httpPort;
	
//	@RequestMapping("/ui")
//	public void uiIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		response.sendRedirect("/ui/index.html");
//	}
//	
//	@RequestMapping("/ui/{name}")
//	public void ui(HttpServletRequest request, HttpServletResponse response,
//			@PathVariable("name") String name) throws IOException {
//		
//		byte[] bytes = getContent(name);
//		response.getOutputStream().write(bytes);
//	}
//	
//	@RequestMapping("/ui/{dir}/{name}")
//	public void ui(HttpServletRequest request, HttpServletResponse response,
//			@PathVariable("dir") String dir,
//			@PathVariable("name") String name) throws IOException {
//		
//		byte[] bytes = getContent(dir + "/" + name);
//		response.getOutputStream().write(bytes);
//	}
//
//	@RequestMapping("/ui/{dir}/{dir2}/{name}")
//	public void ui(HttpServletRequest request, HttpServletResponse response,
//			@PathVariable("dir") String dir,
//			@PathVariable("dir2") String dir2,
//			@PathVariable("name") String name) throws IOException {
//		
//		byte[] bytes = getContent(dir + "/" + dir2 + "/" + name);
//		response.getOutputStream().write(bytes);
//	}
//	
	@RequestMapping("/_plugin/head")
	public void headIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("/_plugin/head/index.html");
	}
	
	@RequestMapping("/_plugin/head/{name}")
	public void head(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("name") String name) throws IOException {
		
		byte[] bytes = getContent("_plugin/head/" + name);
		if (StringUtils.equals(name, "index.html")) {
			URL url = new URL(request.getRequestURL().toString());
			String protocol = url.getProtocol();
			String host = url.getHost();
			String replacement = String.format("var base_uri = \"%s://%s:%s/\";", protocol, host, httpPort);
			
			String s = new String(bytes);
			s = StringUtils.replace(s, SCRIPT, replacement);
			
			bytes = s.getBytes();
		}
		
		response.getOutputStream().write(bytes);
	}
//	
//	@RequestMapping("/_plugin/head/{dir}/{name}")
//	public void head(HttpServletRequest request, HttpServletResponse response,
//			@PathVariable("dir") String dir,
//			@PathVariable("name") String name) throws IOException {
//		
//		byte[] bytes = getContent("_plugin/head/" + dir + "/" + name);
//		response.getOutputStream().write(bytes);
//	}
	
	private byte[] getContent(String path) throws IOException {
		if (!contents.containsKey(path)) {
			InputStream stream = null;
			Reader reader = null;
			try {
				stream = getClass().getClassLoader().getResourceAsStream("META-INF/resources/" + path);
				reader = new InputStreamReader(stream);
				byte[] buff = IOUtils.toByteArray(reader);
				contents.put(path, buff);
			} finally {
				try {reader.close();} catch (Exception e) {}
				try {stream.close();} catch (Exception e) {}
			}
		}
		return contents.get(path);
	}

}
