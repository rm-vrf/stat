package cn.batchfile.stat.agent.domain;

import com.alibaba.fastjson.annotation.JSONField;

public class HttpResult {
	private String url;
	private String method;
	private String user;
	private String password;
	private int code;
	private String message;
	@JSONField(name="cost_time")
	private int costTime;
	@JSONField(name="content_type")
	private String contentType;
	private String charset;
	private String response;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCostTime() {
		return costTime;
	}
	
	public void setCostTime(int costTime) {
		this.costTime = costTime;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getCharset() {
		return charset;
	}
	
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public String getResponse() {
		return response;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
}
