package cn.batchfile.stat.domain;

import java.util.ArrayList;
import java.util.List;

public class Choreo {

	private String app;
	private int scale;
	private String query;
	private List<String> distribution = new ArrayList<String>();

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

	public List<String> getDistribution() {
		return distribution;
	}

	public void setDistribution(List<String> distribution) {
		this.distribution = distribution;
	}

}
