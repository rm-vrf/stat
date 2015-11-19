package cn.batchfile.stat.server.domain;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class GcData {
	@JSONField(name="command_id")
	private String commandId;
	@JSONField(name="agent_id")
	private String agentId;
	private Date time;
	private long pid;
	private double s0c;
	private double s1c;
	private double s0u;
	private double s1u;
	private double ec;
	private double eu;
	private double oc;
	private double ou;
	private double mc;
	private double mu;
	private double ccsc;
	private double ccsu;
	private int ygc;
	private double ygct;
	private int fgc;
	private double fgct;
	private double gct;

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public double getS0c() {
		return s0c;
	}

	public void setS0c(double s0c) {
		this.s0c = s0c;
	}

	public double getS1c() {
		return s1c;
	}

	public void setS1c(double s1c) {
		this.s1c = s1c;
	}

	public double getS0u() {
		return s0u;
	}

	public void setS0u(double s0u) {
		this.s0u = s0u;
	}

	public double getS1u() {
		return s1u;
	}

	public void setS1u(double s1u) {
		this.s1u = s1u;
	}

	public double getEc() {
		return ec;
	}

	public void setEc(double ec) {
		this.ec = ec;
	}

	public double getEu() {
		return eu;
	}

	public void setEu(double eu) {
		this.eu = eu;
	}

	public double getOc() {
		return oc;
	}

	public void setOc(double oc) {
		this.oc = oc;
	}

	public double getOu() {
		return ou;
	}

	public void setOu(double ou) {
		this.ou = ou;
	}

	public double getMc() {
		return mc;
	}

	public void setMc(double mc) {
		this.mc = mc;
	}

	public double getMu() {
		return mu;
	}

	public void setMu(double mu) {
		this.mu = mu;
	}

	public double getCcsc() {
		return ccsc;
	}

	public void setCcsc(double ccsc) {
		this.ccsc = ccsc;
	}

	public double getCcsu() {
		return ccsu;
	}

	public void setCcsu(double ccsu) {
		this.ccsu = ccsu;
	}

	public int getYgc() {
		return ygc;
	}

	public void setYgc(int ygc) {
		this.ygc = ygc;
	}

	public double getYgct() {
		return ygct;
	}

	public void setYgct(double ygct) {
		this.ygct = ygct;
	}

	public int getFgc() {
		return fgc;
	}

	public void setFgc(int fgc) {
		this.fgc = fgc;
	}

	public double getFgct() {
		return fgct;
	}

	public void setFgct(double fgct) {
		this.fgct = fgct;
	}

	public double getGct() {
		return gct;
	}

	public void setGct(double gct) {
		this.gct = gct;
	}
}
