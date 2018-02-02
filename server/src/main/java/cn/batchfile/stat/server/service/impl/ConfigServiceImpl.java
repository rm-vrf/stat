//package cn.batchfile.stat.server.service.impl;
//
//import java.util.Date;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import cn.batchfile.stat.server.dao.ConfigDao;
//import cn.batchfile.stat.server.service.ConfigService;
//
//@Service
//public class ConfigServiceImpl implements ConfigService {
//
//	@Autowired
//	private ConfigDao configDao;
//	
//	@Override
//	public int getInteger(String name) {
//		return configDao.getInteger(name);
//	}
//
//	@Override
//	public String getString(String name) {
//		return configDao.getString(name);
//	}
//
//	@Override
//	public Date getDate(String name) {
//		return configDao.getDate(name);
//	}
//
//	@Override
//	public void setInteger(String name, int value) {
//		configDao.setInteger(name, value);
//	}
//
//	@Override
//	public void setString(String name, String value) {
//		configDao.setString(name, value);
//	}
//
//	@Override
//	public void setDate(String name, Date value) {
//		configDao.setDate(name, value);
//	}
//
//}
