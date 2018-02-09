package cn.batchfile.stat.util;

import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

public class Lock {

	public Lock(RandomAccessFile randomAccessFile, FileLock fileLock) {
		this.randomAccessFile = randomAccessFile;
		this.fileLock = fileLock;
	}
	
	private RandomAccessFile randomAccessFile;
	private FileLock fileLock;
	
	public RandomAccessFile getRandomAccessFile() {
		return randomAccessFile;
	}
	
	public void setRandomAccessFile(RandomAccessFile randomAccessFile) {
		this.randomAccessFile = randomAccessFile;
	}
	
	public FileLock getFileLock() {
		return fileLock;
	}
	
	public void setFileLock(FileLock fileLock) {
		this.fileLock = fileLock;
	}
}
