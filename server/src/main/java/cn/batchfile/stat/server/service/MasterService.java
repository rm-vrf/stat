package cn.batchfile.stat.server.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.batchfile.stat.server.domain.Master;
import cn.batchfile.stat.server.util.YamlUtils;

@org.springframework.stereotype.Service
public class MasterService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MasterService.class);
    private AtomicBoolean master = new AtomicBoolean(false);
	
	@Autowired
	private ZookeeperService zookeeperService;
	
	@PostConstruct
	public void init() {
        //create temp node after /master
		String path = ZookeeperService.MASTER_PATH + "/stat-hub-";
		String data = getData();
		String self = zookeeperService.createEphemeralSequential(path, data);
		
		//elect master
		List<String> list = zookeeperService.getChildren(ZookeeperService.MASTER_PATH, (n, l) -> {
			electMaster(self, l);
		});
		electMaster(self, list);
	}

    /**
     * 是否是主节点
     * @return 主节点
     */
    public boolean isMaster() {
        return master.get();
    }

    private String getData() {
    	Master master = new Master();
    	master.setAddress(getAddress());
    	master.setHostname(getHostname());
    	master.setStartTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss.SSSZ").format(new Date()));
        return YamlUtils.toString(master);
    }
    
    private void electMaster(String self, List<String> nodes) {
        Collections.sort(nodes);
        master.set(nodes.size() > 0 && nodes.get(0).equals(self));
        LOG.info("master elected: {}", master.get());
    }

    private String getHostname() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String name = inetAddress.getHostName();
            return name;
        } catch (UnknownHostException e) {
            return StringUtils.EMPTY;
        }
    }

    private String getAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces
                        .nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address) {
                        return ia.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            LOG.error("unable to get current IP " + e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

}
