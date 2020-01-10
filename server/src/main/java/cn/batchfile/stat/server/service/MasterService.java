package cn.batchfile.stat.server.service;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import cn.batchfile.stat.server.dao.MasterRepository;
import cn.batchfile.stat.server.domain.system.Master;
import cn.batchfile.stat.server.dto.MasterTable;

@org.springframework.stereotype.Service
public class MasterService {
	private static final Logger LOG = LoggerFactory.getLogger(MasterService.class);
    private AtomicBoolean master = new AtomicBoolean(false);
    private String hostname;
    private Map<String, Integer> offlineCounts = new ConcurrentHashMap<String, Integer>();
    
    @Value("${server.address:}")
    private String address;
    
    @Value("${server.port:0}")
    private Integer port;
    
    @Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	private MasterRepository masterRepository;
	
	@PostConstruct
	public void init() {
		//设置进程标识数据
		hostname = getHostname();
		if (port == 0) {
			port = getPid();
		}
		if (StringUtils.isEmpty(address)) {
			address = getAddress();
		}
		LOG.info("STAT HUB START, ADDRESS: {}, PORT: {}", address, port);

		//创建数据对象
		MasterTable mt = new MasterTable();
		mt.setAddress(address);
		mt.setHostname(hostname);
		mt.setId(String.format("%s:%s", address, port));
		mt.setStartTime(new Date());
		
		//启动定时器，检查所有节点的状态，判断自己是不是master
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
			try {
				masterRepository.save(mt);
				checkHosts(mt.getId());
			} catch (Exception e) {
				LOG.error("error when check master", e);
			}
		}, 10, 10, TimeUnit.SECONDS);
	}

    public boolean isMaster() {
        return master.get();
    }
    
    public List<Master> getMasters() {
    	List<Master> masters = new ArrayList<Master>();
    	Iterable<MasterTable> iter = masterRepository.findMany();
    	iter.forEach(it -> {
    		Master master = new Master();
    		master.setAddress(it.getAddress());
    		master.setHostname(it.getHostname());
    		master.setId(it.getId());
    		master.setStartTime(it.getStartTime());
    		masters.add(master);
    	});
    	return masters;
    }
    
    private void checkHosts(String self) {
    	List<MasterTable> ms = new ArrayList<MasterTable>();
    	Iterable<MasterTable> iter = masterRepository.findMany();
    	iter.forEach(it -> {
    		String id = it.getId();
    		if (connectOk(id)) {
    			ms.add(it);
    			offlineCounts.put(id, 0);
    		} else {
    			int count = offlineCounts.containsKey(id) ? offlineCounts.get(id) + 1 : 1;
    			offlineCounts.put(id, count);
    			if (offlineCounts.get(id) > 3) {
    				masterRepository.deleteById(id);
    			}
    		}
    	});

    	//如果第一个节点就是自己，本人就是主节点
    	boolean oldStatus = master.get();
    	master.set(ms.size() > 0 && StringUtils.equals(ms.get(0).getId(), self));
    	if (master.get() != oldStatus) {
    		LOG.info("==== ELECTED MASTER =====");
    	}
    }
    
    private boolean connectOk(String serviceId) {
    	try {
    		String url = "http://" + serviceId + "/api/ping";
    		String ok = restTemplate.getForObject(url, String.class);
    		return StringUtils.equalsIgnoreCase(ok, "ok");
    	} catch (Exception e) {
    		return false;
    	}
    }

    private int getPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
        String s = StringUtils.split(name, '@')[0];
        return Integer.valueOf(s);
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
