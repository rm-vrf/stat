package cn.batchfile.stat.agent.service.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;

import cn.batchfile.stat.agent.Main;
import cn.batchfile.stat.agent.domain.State;
import cn.batchfile.stat.agent.service.StateService;

public class StateServiceImpl implements StateService {
	private State state;
	
	public void init() throws IOException {
		state = new State();
		state.setAddress(StringUtils.isEmpty(Main.address) ? get_address() : Main.address);
		state.setAgentId(Main.read_config().getProperty("agent.id").toString());
		state.setHostname(get_host());
		state.setPort(Main.port);
		state.setStartTime(new Date());
	}
	
	@Override
	public State getState() {
		return state;
	}

	private String get_host() throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getLocalHost();
		String host = inetAddress.getHostName();
		return host;
	}
	
	private String get_address() throws SocketException {
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
			Enumeration<InetAddress> nias = ni.getInetAddresses();
			while (nias.hasMoreElements()) {
				InetAddress ia = (InetAddress) nias.nextElement();
				if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()) {
					String address = ia.getHostAddress();
					if (StringUtils.isNotEmpty(address)) {
						return address;
					}
				}
			}
		}
		return null;
	}
}
