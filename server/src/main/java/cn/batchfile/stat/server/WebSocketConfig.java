package cn.batchfile.stat.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import cn.batchfile.stat.server.controller.LogSocketHandler;
import cn.batchfile.stat.server.controller.TerminalSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new TerminalSocketHandler(), "/ws/terminal/**");
        registry.addHandler(new LogSocketHandler(), "/ws/log/**");
    }
}
