//package cn.batchfile.stat.domain;
//
//import org.apache.commons.lang.StringUtils;
//
//import com.alibaba.fastjson.JSON;
//
//public class P {
//	private long pid;
//	private String node;
//	private String app;
//
//	public P() {
//		//pass
//	}
//	
//	public P(long pid, String node, String app) {
//		this.pid = pid;
//		this.node = node;
//		this.app = app;
//	}
//	
//	public long getPid() {
//		return pid;
//	}
//
//	public void setPid(long pid) {
//		this.pid = pid;
//	}
//
//	public String getNode() {
//		return node;
//	}
//
//	public void setNode(String node) {
//		this.node = node;
//	}
//
//	public String getApp() {
//		return app;
//	}
//
//	public void setApp(String app) {
//		this.app = app;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		return StringUtils.equals(JSON.toJSONString(this), JSON.toJSONString(obj));
//	}
//}
