package cn.batchfile.stat.server.service;

import java.util.Date;

public interface ConfigService {

	int getInteger(String name);
	
	String getString(String name);
	
	Date getDate(String name);

	void setInteger(String name, int value);
	
	void setString(String name, String value);
	
	void setDate(String name, Date value);
}
