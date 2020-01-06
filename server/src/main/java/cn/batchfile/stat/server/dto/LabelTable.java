package cn.batchfile.stat.server.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "Label")
@Table(name = "label")
public class LabelTable {
	
	@Id
	@Column(length = 64)
    private String id;
	
	@Column(length = 64, name = "node_id")
    private String nodeId;
	
	@Column(length = 64)
	private String label;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
