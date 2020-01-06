package cn.batchfile.stat.server.exception;

public class NotEmptyException extends RuntimeException {

	private static final long serialVersionUID = 3774183185591808926L;
	
	public NotEmptyException(String message) {
		super(message);
	}

}
