package cn.batchfile.stat.server.exception;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 6796040738741157112L;
	
	public NotFoundException(String message) {
		super(message);
	}

}
