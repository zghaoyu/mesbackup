package com.cncmes.dto;

/**
 * @author LI ZI LONG
 * the Data Transfer Object for database access
 *
 */
public class CNCMachiningProcess {
	private int id;
	private String processname;
	private int processcode;
	private String description;
	private String realTableName = "cnc_machiningprocess";
	public int getProcesscode() {
		return processcode;
	}
	public void setProcesscode(int processcode) {
		this.processcode = processcode;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getProcessname() {
		return processname;
	}
	public void setProcessname(String processname) {
		this.processname = processname;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String desc) {
		this.description = desc;
	}
	@Override
	public String toString() {
		return "CNCMachiningProcess [id=" + id + ", processname=" + processname + ", processcode=" + processcode
				+ ", description=" + description + ", realTableName=" + realTableName + "]";
	}
	public CNCMachiningProcess(int id, String processname, String desc) {
		super();
		this.id = id;
		this.processname = processname;
		this.description = desc;
	}
	public CNCMachiningProcess() {
		super();
	}
	
}
