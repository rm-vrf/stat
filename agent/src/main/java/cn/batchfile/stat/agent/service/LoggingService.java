package cn.batchfile.stat.agent.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

@Service
public class LoggingService {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LoggingService.class);
	private Map<String, Logger> loggers = new ConcurrentHashMap<>();

	public Logger createLogger(String name, Map<String, String> options) {
		if (!loggers.containsKey(name)) {
			synchronized (loggers) {
				if (!loggers.containsKey(name)) {
					LOG.info("create logger, name: {}, options: {}", name, options);
					Logger logger = create(name, options);
					loggers.put(name, logger);
				}
			}
		}
		return loggers.get(name);
	}
	
	public Logger getLogger(String name) {
		return loggers.get(name);
	}

	@SuppressWarnings("unchecked")
	private Logger create(String name, Map<String, String> options) {
		String file = options.get("file");
		String fileNamePattern = options.get("file-name-pattern");
		String maxHistory = options.get("max-history");
		
		LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
		logEncoder.setContext(logCtx);
		logEncoder.setPattern("%msg%n");
		logEncoder.start();
		
		@SuppressWarnings("rawtypes")
		RollingFileAppender logFileAppender = new RollingFileAppender();
		logFileAppender.setContext(logCtx);
		logFileAppender.setName(name);
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
		
		Logger log = logCtx.getLogger(name);
		log.setAdditive(false);
		log.setLevel(Level.INFO);
		log.addAppender(logFileAppender);
		
		return log;
	}
}
