package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import cn.batchfile.stat.server.domain.node.Node;
import cn.batchfile.stat.server.service.NodeService;

@Component
public class TerminalSocketHandler extends TextWebSocketHandler {
	public static NodeService nodeService;
	private static final Logger LOG = LoggerFactory.getLogger(TerminalSocketHandler.class);
	private Map<String, Socket> sockets = null;

	public TerminalSocketHandler() {
		LOG.info("+++ create TerminalSocketHandler object +++");
		sockets = new ConcurrentHashMap<String, Socket>();
	}
	
	@Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		LOG.info("afterConnectionEstablished: {}, {}", session.getUri(), session.getAttributes());
		String[] ary = StringUtils.split(session.getUri().toString(), '/');
		String containerId = ary[ary.length - 1];
		String nodeId = ary[ary.length - 2];
		LOG.info("nodeId: {}, containerId: {}", nodeId, containerId);
		Node node = nodeService.getNode(nodeId);

		// connect to docket
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://" + node.getInfo().getDockerHost())
                .withApiVersion(node.getApiVersion()).build();
        DockerClient docker = DockerClientBuilder.getInstance(config).build();
        
        // create exec on container
        String execId = docker.execCreateCmd(containerId)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                //.withCmd("/bin/sh", "-c", "TERM=xterm-256color; export TERM; [ -x /bin/bash ] && ([ -x /usr/bin/script ] && /usr/bin/script -q -c \"/bin/bash\" /dev/null || exec /bin/bash) || exec /bin/sh")
                .withCmd("/bin/sh")
                .withTty(true)
                .exec().getId();
        LOG.info("create exec, id: {}", execId);

        
        // create socket channel
        ary = StringUtils.split(StringUtils.remove(node.getInfo().getDockerHost(), '/'), ':');
        Socket socket = new Socket(ary[0], Integer.valueOf(ary[1]));
        sockets.put(session.getId(), socket);
        
        // send http request
        String uri = "/v" + node.getApiVersion() + "/exec/" + execId + "/start";
        String data = "{\"Detach\":false,\"Tty\":true}";
        OutputStream out = socket.getOutputStream();
        List<String> lines = new ArrayList<>();
        lines.add("POST " + uri + " HTTP/1.1");
        lines.add("host: " + node.getInfo().getDockerHost());
        lines.add("user-agent: curl/7.55.1");
        lines.add("accept: */*");
        lines.add("content-type: application/json");
        lines.add("content-length: " + data.length());
        lines.add("");
        lines.add(data);
        IOUtils.writeLines(lines, "\n", out, Charset.forName("UTF-8"));
        
        // read output
        InputStream in = socket.getInputStream();
        new Thread(() -> {
            try {
                while (true) {
                    byte[] buff = new byte[1];
                    int i = in.read(buff);
                    if (i > 0) {
                        session.sendMessage(new TextMessage(buff));
                    }
                }
            } catch (Exception e) {}
        }).start();
	}

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {

        String payload = message.getPayload();
        Socket socket = sockets.get(session.getId());
        
        if (socket != null) {
        	OutputStream outputStream = socket.getOutputStream();
        	IOUtils.write(payload.getBytes(Charset.forName("UTF-8")), outputStream);
        	LOG.info("get message from client: {}, {}", payload, this.toString());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        LOG.info("afterConnectionClosed, status: {}");
        Socket socket = sockets.get(session.getId());
        
        if (socket != null) {
	        OutputStream outputStream = socket.getOutputStream();
	        IOUtils.write("exit\n".getBytes(Charset.forName("UTF-8")), outputStream);
	        try {
	        	socket.close();
	        } catch (Exception e) {}
        }
        sockets.remove(session.getId());
    }
}
