package cn.batchfile.stat.agent.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import cn.batchfile.stat.domain.Service;

@org.springframework.stereotype.Service
public class LoggingService {
	
	private static final Logger LOG = LoggerFactory.getLogger(LoggingService.class);
	private static Map<String, Function<Service, Logger>> DRIVERS = new HashMap<String, Function<Service, Logger>>();
	static {
		DRIVERS.put("time-based-rolling-file", LoggingService::createTimeBasedRollingFileLogger);
		DRIVERS.put("file", LoggingService::createTimeBasedRollingFileLogger);
		DRIVERS.put("log-file", LoggingService::createTimeBasedRollingFileLogger);
	};
	private Map<String, Logger> loggers = new ConcurrentHashMap<>();

	public Logger createLogger(Service service) {
		if (!loggers.containsKey(service.getName())) {
			synchronized (loggers) {
				if (!loggers.containsKey(service.getName())) {					
					Logger logger = null;
					
					// 寻找 function，创建 logger
					if (service.getLogging() != null) {
						LOG.info("LOGGING, driver: {}, options: {}", 
								service.getLogging().getDriver(), service.getLogging().getOptions());
						
						Function<Service, Logger> function = DRIVERS.get(service.getLogging().getDriver());
						if (function != null) {
							logger = function.apply(service);
						}
					} 
					
					// 设置 logger
					if (logger == null) {
						loggers.put(service.getName(), LOG);
					} else {
						loggers.put(service.getName(), logger);
					}
				}
			}
		}
		return loggers.get(service.getName());
	}
	
	public Logger getLogger(String name) {
		return loggers.get(name);
	}

	@SuppressWarnings("unchecked")
	protected static Logger createTimeBasedRollingFileLogger(Service service) {
		Map<String, String> options = service.getLogging().getOptions();
		
		String file = options == null ? null : options.get("file");
		String fileNamePattern = options == null ? null : options.get("file-name-pattern");
		String maxHistory = options == null ? null : options.get("max-history");
		
		LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
		logEncoder.setContext(logCtx);
		logEncoder.setPattern("%msg%n");
		logEncoder.start();
		
		@SuppressWarnings("rawtypes")
		RollingFileAppender logFileAppender = new RollingFileAppender();
		logFileAppender.setContext(logCtx);
		logFileAppender.setName(service.getName());
		logFileAppender.setEncoder(logEncoder);
		logFileAppender.setAppend(true);
		if (StringUtils.isNotEmpty(file)) {
			logFileAppender.setFile(file);
		}
		
		@SuppressWarnings("rawtypes")
		TimeBasedRollingPolicy logFilePolicy = new TimeBasedRollingPolicy();
		logFilePolicy.setContext(logCtx);
		logFilePolicy.setParent(logFileAppender);
		if (StringUtils.isNotEmpty(fileNamePattern)) {
			logFilePolicy.setFileNamePattern(fileNamePattern);
		}
		if (StringUtils.isNotEmpty(maxHistory) && StringUtils.isNumeric(maxHistory)) {
			logFilePolicy.setMaxHistory(Integer.valueOf(maxHistory));
		}
		logFilePolicy.start();
		
		logFileAppender.setRollingPolicy(logFilePolicy);
		logFileAppender.start();
		
		ch.qos.logback.classic.Logger log = logCtx.getLogger(service.getName());
		log.setAdditive(false);
		log.setLevel(Level.INFO);
		log.addAppender(logFileAppender);
		
		return log;
	}
}
