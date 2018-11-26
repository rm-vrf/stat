//package cn.batchfile.stat.server;
//
//import java.io.File;
//
//import org.apache.commons.lang.StringUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
//import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
//import org.springframework.stereotype.Component;
//
//@Component
//public class CustomContainer implements EmbeddedServletContainerCustomizer {
//
//	@Value("${document.root:}")
//	private String documentRoot;
//	
//	@Override
//	public void customize(ConfigurableEmbeddedServletContainer container) {
//		if (StringUtils.isNotEmpty(documentRoot)) {
//			container.setDocumentRoot(new File(documentRoot));
//		}
//	}
//}
