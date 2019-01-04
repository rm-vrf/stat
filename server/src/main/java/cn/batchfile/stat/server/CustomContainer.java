package cn.batchfile.stat.server;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomContainer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

	@Value("${document.root:}")
	private String documentRoot;
	
	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		if (!StringUtils.isEmpty(documentRoot)) {
			factory.setDocumentRoot(new File(documentRoot));
		}
	}
}
