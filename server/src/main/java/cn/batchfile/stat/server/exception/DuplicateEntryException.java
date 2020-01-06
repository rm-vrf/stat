package cn.batchfile.stat.server.exception;

public class DuplicateEntryException extends RuntimeException {

	private static final long serialVersionUID = -2070858487710169609L;
	
	public DuplicateEntryException(String message) {
		super(message);
	}

}
