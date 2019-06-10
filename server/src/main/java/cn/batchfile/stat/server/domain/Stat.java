package cn.batchfile.stat.server.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stat {
	private List<Object> keys = new ArrayList<Object>();
	private Map<String, List<Object>> datas = new HashMap<String, List<Object>>();
	
	public List<Object> getKeys() {
		return keys;
	}
	
	public void setKeys(List<Object> keys) {
		this.keys = keys;
	}
	
	public Map<String, List<Object>> getDatas() {
		return datas;
	}
	
	public void setDatas(Map<String, List<Object>> datas) {
		this.datas = datas;
	}
}
