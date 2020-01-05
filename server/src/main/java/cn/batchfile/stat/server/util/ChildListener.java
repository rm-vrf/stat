package cn.batchfile.stat.server.util;

import java.util.List;

public interface ChildListener {
	
	public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception;

}
