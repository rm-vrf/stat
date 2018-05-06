package cn.batchfile.stat.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;

public class Choreo {

	private String app;
	private int scale;
	private String query;
	private List<String> dist = new ArrayList<String>();

	public String getApp() {
		return app;
	}
	
	public void setApp(String app) {
		this.app = app;
	}
	
	public int getScale() {
		return scale;
	}
	
	public void setScale(int scale) {
		this.scale = scale;
	}
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<String> getDist() {
		return dist;
	}

	public void setDist(List<String> dist) {
		this.dist = dist;
	}

	@Override
	public boolean equals(Object obj) {
		return StringUtils.equals(JSON.toJSONString(this), JSON.toJSONString(obj));
	}
}
