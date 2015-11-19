package cn.batchfile.stat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class HttpClient {
	private static String LINE_SEPERATOR = System.getProperty("line.separator", "\n");
	private int connectionTimeout = 0;
	private int readTimeout = 0;
	private String contentType;
	private String charset;
	private int responseCode;
	private String responseMessage;
	
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	
	public int getReadTimeout() {
		return readTimeout;
	}
	
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
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
	
	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public String head(String url) {
		return request(url, StringUtils.EMPTY, "HEAD");
	}
	
	public String get(String url) {
		return request(url, StringUtils.EMPTY, "GET");
	}
	
	public String put(String url, String data) {
		return request(url, data, "PUT");
	}
	
	public String post(String url, String data) {
		return request(url, data, "POST");
	}

	public String delete(String url, String data) {
		return request(url, data, "DELETE");
	}
	
	private String request(String url, String data, String method) {
		OutputStream outputStream = null;
		InputStream inputStream = null;
		HttpURLConnection conn = null;
		try {
			URL urlObject = new URL(url);
			conn = (HttpURLConnection)urlObject.openConnection();
			conn.setRequestMethod(method);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			if (connectionTimeout > 0) {
				conn.setConnectTimeout(connectionTimeout);
			}
			if (readTimeout > 0) {
				conn.setReadTimeout(readTimeout);
			}
			
			if (!StringUtils.isBlank(data)) {
				conn.setRequestProperty("Content-Type", String.format("%s;charset=%s", contentType, charset));
				conn.addRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
				outputStream = conn.getOutputStream();
				IOUtils.write(data, outputStream, charset);
				outputStream.flush();
			}
			
			//判断返回编码
			responseCode = conn.getResponseCode();
			if (responseCode < 200 || responseCode >= 300) {
				//IOUtils.closeQuietly(conn.getInputStream());
				IOUtils.closeQuietly(inputStream);
				IOUtils.closeQuietly(outputStream);
				responseMessage = conn.getResponseMessage();
				throw new RuntimeException(String.format("error when post http request, code: %s, message: %s", responseCode, responseMessage));
			}
			
			//读取Web返回结果
			Map<String, String> headers = getHeaders(conn);
			inputStream = conn.getInputStream();
			
			charset = getCharsetFromHeaders(headers);
			contentType = getContentTypeFromHeader(headers); 
			
			List<String> lines = IOUtils.readLines(inputStream, charset);
			return StringUtils.join(lines, LINE_SEPERATOR);
		} catch (MalformedURLException e) {
			throw new RuntimeException("error when post http request", e);
		} catch (IOException e) {
			throw new RuntimeException("error when post http request", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
			conn.disconnect();
		}
	}
	
	private String getContentTypeFromHeader(Map<String, String> headers) {
		String ct = "text/plain";
		if (headers.containsKey("Content-Type")) {
			ct = headers.get("Content-Type");
			if (StringUtils.contains(ct, ";")) {
				ct = StringUtils.substringBefore(ct, ";");
			}
		}
		return ct;
	}
	
	private String getCharsetFromHeaders(Map<String, String> headers) {
		String charset = "utf-8";
		
		if (!headers.containsKey("Content-Type")) {
			return charset;
		}
		
		String contentType = headers.get("Content-Type");
		
		String[] parts = StringUtils.split(contentType, ";");
		for (String part : parts) {
			String[] keyValue = StringUtils.split(part, "=");
			if (keyValue.length > 1) {
				String key = StringUtils.trim(keyValue[0]);
				String value = StringUtils.trim(keyValue[1]);
				if (StringUtils.equals(key, "charset")) {
					charset = value;
					break;
				}
			}
		}
	
		//这里最好是使用标准charset检查，防止charset错误
		//不过不检查也没关系，请求字符串写错了是客户端的职责，服务端不检查也是正常的，只需要有异常抛出
		return charset;
	}
	
	private Map<String, String> getHeaders(URLConnection connection) {
		Map<String, String> headers = new HashMap<String, String>();
		String key;
		int i = 1;
		while ((key = connection.getHeaderFieldKey(i)) != null) {
			String value = connection.getHeaderField(i); 
			headers.put(key, value);
			
			i++;
		}
		return headers;
	}
}
