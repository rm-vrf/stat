//package cn.batchfile.stat.server.controller;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.net.URL;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import cn.batchfile.stat.server.service.ElasticService;
//
//@Controller
//public class HeadController {
//	private static final String JS = "var base_uri = location.href.replace(/_plugin\\/.*/, '');";
//	private String html = StringUtils.EMPTY;
//	
//	@Autowired
//	private ElasticService elasticService;
//	
//	@RequestMapping("/_plugin/head")
//	public void redir(HttpServletResponse response) throws IOException {
//		response.sendRedirect("/_plugin/head/");
//	}
//	
//	@RequestMapping("/_plugin/head/")
//	public void head(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		if (StringUtils.isEmpty(html)) {
//			URL url = new URL(request.getRequestURL().toString());
//			String protocol = url.getProtocol();
//			String host = url.getHost();
//			
//			InputStream stream = null;
//			Reader reader = null;
//			try {
//				stream = getClass().getClassLoader().getResourceAsStream("META-INF/resources/_plugin/head/index.html");
//				reader = new InputStreamReader(stream);
//				html = IOUtils.toString(reader);
//
//				String replacement = String.format("var base_uri = \"%s://%s:%s/\";", protocol, host, elasticService.elasticHttpPort);
//				html = StringUtils.replace(html, JS, replacement);
//			} finally {
//				try {reader.close();} catch (Exception e) {}
//				try {stream.close();} catch (Exception e) {}
//			}
//		}
//
//		response.getOutputStream().write(html.getBytes());
//	}
//}
