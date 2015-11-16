package cn.batchfile.stat.agent.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import cn.batchfile.stat.agent.domain.DatabaseResult;
import cn.batchfile.stat.agent.domain.HttpResult;
import cn.batchfile.stat.agent.service.TestService;
import cn.batchfile.stat.util.HttpClient;
import cn.batchfile.stat.util.JsonUtil;

public class TestServiceImpl implements TestService {

	@Override
	public HttpResult testHttp(String url, String method, String username, String password) {
		HttpResult hr = new HttpResult();
		hr.setUrl(url);
		hr.setMethod(method);
		hr.setUser("-");
		hr.setPassword("*");
		
		HttpClient hc = new HttpClient();
		hc.setConnectionTimeout(10000);
		hc.setReadTimeout(20000);
		
		String body = null;
		long begin = new Date().getTime();

		try {
			if (StringUtils.equalsIgnoreCase(method, "get")) {
				body = hc.get(url);
			} else if (StringUtils.equalsIgnoreCase(method, "put")) {
				body = hc.put(url, null);
			} else if (StringUtils.equalsIgnoreCase(method, "post")) {
				body = hc.post(url, null);
			} else if (StringUtils.equalsIgnoreCase(method, "delete")) {
				body = hc.delete(url, null);
			} else if (StringUtils.equalsIgnoreCase(method, "head")) {
				body = hc.head(url);
			}
			
			hr.setCharset(hc.getCharset());
			hr.setCode(hc.getResponseCode());
			hr.setContentType(hc.getContentType());
			hr.setMessage(hc.getResponseMessage());
			hr.setResponse(body);
		} catch (Exception e) {
			hr.setCode(0);
			hr.setMessage(e.getMessage());
		} finally {
			hr.setCostTime((int)(new Date().getTime() - begin));
		}
		return hr;
	}

	@Override
	public DatabaseResult testDatabase(String driver, String url, String username, String password, String sql) {
		DatabaseResult dr = new DatabaseResult();
		dr.setDriver(driver);
		dr.setUrl(url);
		dr.setUser("-");
		dr.setPassword("*");
		dr.setSql(sql);
		
		long begin = new Date().getTime();
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(url, username, password);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); 
			ResultSetMetaData meta = rs.getMetaData();
			int count = 0;
			while (rs.next() && count ++ < 10) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 1; i <= meta.getColumnCount(); i ++) {
					map.put(meta.getColumnName(i), rs.getObject(i));
				}
				list.add(map);
			}
			dr.setOk(true);
			dr.setResponse(JsonUtil.encode(list));
		} catch (Exception e) {
			dr.setOk(false);
			dr.setResponse(e.getMessage());
		} finally {
			dr.setCostTime((int)(new Date().getTime() - begin));
		}
		
		return dr;
	}
}
