package com.cncmes.dto;

/**
 * @author LI ZI LONG
 * the Data Transfer Object for database access
 *
 */
public class CNCLines {
	private String realTableName = "cnc_lines";
	private int id;
	private String linename;
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
	public String getLinename() {
		return linename;
	}
	public void setLinename(String linename) {
		this.linename = linename;
	}
	@Override
	public String toString() {
		return "CNCLines [id=" + id + ", linename=" + linename + "]";
	}
	public CNCLines(int id, String linename) {
		super();
		this.id = id;
		this.linename = linename;
	}
	public CNCLines() {
		super();
	}
}
