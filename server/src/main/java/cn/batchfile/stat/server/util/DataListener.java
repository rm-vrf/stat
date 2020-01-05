package cn.batchfile.stat.server.util;

public interface DataListener {
	
	public void handleDataChange(boolean delete, String dataPath, String data) throws Exception;

}
