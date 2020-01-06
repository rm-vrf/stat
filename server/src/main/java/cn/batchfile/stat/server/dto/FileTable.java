package cn.batchfile.stat.server.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "File")
@Table(name = "file")
public class FileTable {
	public static final String TYPE_NAMESPACE = "ns";
	public static final String TYPE_DIRECTORY = "d";
	public static final String TYPE_FILE = "f";

	@Id
	@Column(length = 64)
	private String id;
	
	@Column(length = 64)
	private String namespace;
	
	@Column(length = 255)
	private String name;
	
	@Column(length = 64)
	private String parent;
	
	@Column
	private Long size;

	@Column
	private Date timestamp;
	
	@Column(length = 4)
	private String type;

	@Column(length = 255)
	private String file;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
