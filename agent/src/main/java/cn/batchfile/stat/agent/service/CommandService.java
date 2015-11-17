package cn.batchfile.stat.agent.service;

public interface CommandService {

	String execute(String cmd);
	
	String start(String cmd);
	
	String consume(String id);
	
	void terminate(String id);

	String getStatus(String id);
}
