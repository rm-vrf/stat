package cn.batchfile.stat.agent;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import cn.batchfile.stat.service.ServiceService;

@SpringBootApplication
public class Main {
	
	@Value("${store.directory}")
	private String storeDirectory;
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.setConnectTimeout(10000).setReadTimeout(10000).build();
	}
	
	@Bean
	public ServiceService serviceService() throws IOException {
		ServiceService ss = new ServiceService();
		ss.setStoreDirectory(storeDirectory);
		return ss;
	}
	
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
