package cn.batchfile.stat.server.controller;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import cn.batchfile.stat.server.domain.node.Node;
import cn.batchfile.stat.server.service.NodeService;

@Component
public class LogSocketHandler extends TextWebSocketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(LogSocketHandler.class);
	public static NodeService nodeService;
	private Map<String, ConnectionHandler> handlers = null;
	
	public LogSocketHandler() {
		LOG.info("+++ create LogSocketHandler object +++");
		
		handlers = new ConcurrentHashMap<>();
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
			LOG.debug("clear websocket session");
			for(Iterator<Map.Entry<String, ConnectionHandler>> it = handlers.entrySet().iterator(); it.hasNext(); ) {
			    Map.Entry<String, ConnectionHandler> entry = it.next();
			    if(System.currentTimeMillis() - entry.getValue().timestamp > 86400000) {
			    	LOG.info("remove idle session: {}", entry.getKey());
			    	exitQuielty(entry.getKey(), StringUtils.EMPTY);
			        it.remove();
			    }
			}
		}, 60, 60, TimeUnit.SECONDS);
	}
	
	@Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		LOG.info("afterConnectionEstablished: {}, {}", session.getUri(), session.getAttributes());
		String[] ary = StringUtils.split(session.getUri().toString(), '/');
		String containerId = ary[ary.length - 1];
		String nodeId = ary[ary.length - 2];
		LOG.info("nodeId: {}, containerId: {}", nodeId, containerId);
		Node node = nodeService.getNode(nodeId);
		
		// create socket channel
		ary = StringUtils.split(StringUtils.remove(node.getInfo().getDockerHost(), '/'), ':');
		Socket socket = new Socket(ary[0], Integer.valueOf(ary[1]));
		ConnectionHandler handler = new ConnectionHandler();
        handler.socket = socket;
        handler.timestamp = System.currentTimeMillis();
        handler.session = session;
        handlers.put(session.getId(), handler);

        // send http request
        String uri = "/v" + node.getApiVersion() + "/containers/" + containerId + "/logs?follow=true&stdout=true&stderr=true&tail=100";
        OutputStream out = socket.getOutputStream();
        List<String> lines = new ArrayList<>();
        lines.add("GET " + uri + " HTTP/1.1");
        lines.add("host: " + node.getInfo().getDockerHost());
        lines.add("user-agent: curl/7.55.1");
        lines.add("accept: */*");
        lines.add("content-type: application/json");
        lines.add("content-length: 0");
        lines.add("");
        IOUtils.writeLines(lines, "\n", out, Charset.forName("UTF-8"));
        
        // read output
        InputStream in = socket.getInputStream();
        new Thread(() -> {
        	try {
        		while (socket.isConnected()) {
        			byte[] buff = new byte[1];
        			int i = in.read(buff);
        			if (i > 0) {
        				session.sendMessage(new TextMessage(buff));
        				handler.timestamp = System.currentTimeMillis();
        			}
        		}
            } catch (SocketException e) {
            	LOG.info("error when read socket", e);
            } catch (Exception e) {
            	LOG.error("error when read socket", e);
            } finally {
            	closeQuielty(socket);
            }
        }).start();;
	}
	
	@Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		LOG.info("afterConnectionClosed, status: {}", status);
		exitQuielty(session.getId(), "\r\nSESSION CLOSED " + status);
		handlers.remove(session.getId());
	}

    private void exitQuielty(String sessionId, String message) {
    	ConnectionHandler handler = handlers.get(sessionId);
    	if (handler != null) {
    		//关闭exec连接
    		closeQuielty(handler.socket);

    		//向控制台发出信息
    		try {
        		handler.session.sendMessage(new TextMessage(message));
    		} catch (Exception e) {}
    	}
    }
    
    private void closeQuielty(Closeable closeable) {
    	try {
    		closeable.close();
    	} catch (Exception e) {}
    }
    
    class ConnectionHandler {
    	protected WebSocketSession session;
    	protected long timestamp;
    	protected Socket socket;
    }
}
