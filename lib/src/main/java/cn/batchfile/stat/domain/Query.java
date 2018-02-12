package cn.batchfile.stat.domain;

public class Query {
	private int from = 0;
	private int size = 20;
	private String query = "*";
	
	public int getFrom() {
		return from;
	}
	
	public void setFrom(int from) {
		this.from = from;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
}
