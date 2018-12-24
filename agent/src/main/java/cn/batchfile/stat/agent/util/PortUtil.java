package cn.batchfile.stat.agent.util;

import java.io.IOException;
import java.net.ServerSocket;

public class PortUtil {

	/**
	 * 检测端口是否被占用
	 * 
	 * @param port
	 *            端口号
	 * @return 端口被占用返回true，否则返回false
	 */
	public static boolean isUsedPort(int port) {
		boolean isUsedPort = true;
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			isUsedPort = false;
		} catch (Exception e) {
		} finally {
			if (null != serverSocket) {
				try {
					serverSocket.close();
				} catch (IOException e) {
				}
			}
		}
		return isUsedPort;
	}
}
