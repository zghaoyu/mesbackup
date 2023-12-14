package com.cncmes.dto;

/**
 * @author LI ZI LONG
 * the Data Transfer Object for database access
 *
 */
public class CNCScanners {
	private int id;
	private String scanner_model;
	private String driver;
	private String cmdendchr;
	private String realTableName = "cnc_scanners";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getScanner_model() {
		return scanner_model;
	}
	public void setScanner_model(String scanner_model) {
		this.scanner_model = scanner_model;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public String getCmdEndChr() {
		return cmdendchr;
	}
	public void setCmdEndChr(String cmdendchr) {
		this.cmdendchr = cmdendchr;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	@Override
	public String toString() {
		return "CNCScanners [id=" + id + ", scanner_model=" + scanner_model + ", driver=" + driver+ ", cmdendchr=" + cmdendchr + ", realTableName="
				+ realTableName + "]";
	}
	public CNCScanners() {
		super();
	}
	public CNCScanners(int id, String scanner_model, String driver, String cmdendchr, String realTableName) {
		super();
		this.id = id;
		this.scanner_model = scanner_model;
		this.driver = driver;
		this.cmdendchr = cmdendchr;
		this.realTableName = realTableName;
	}
}
