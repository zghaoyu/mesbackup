package com.cncmes.dto;

/**
 * @author LI ZI LONG
 * the Data Transfer Object for database access
 *
 */
public class CNCScannersInfo {
	private int id;
	private String scanner_model;
	private String scanner_ip;
	private String scanner_port;
	private String realTableName = "cnc_scannersinfo";
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
	public String getScanner_model() {
		return scanner_model;
	}
	public void setScanner_model(String scanner_model) {
		this.scanner_model = scanner_model;
	}
	public String getScanner_ip() {
		return scanner_ip;
	}
	public void setScanner_ip(String scanner_ip) {
		this.scanner_ip = scanner_ip;
	}
	public String getScanner_port() {
		return scanner_port;
	}
	public void setScanner_port(String scanner_port) {
		this.scanner_port = scanner_port;
	}
	@Override
	public String toString() {
		return "CNCScannersInfo [id=" + id + ", scanner_model=" + scanner_model + ", scanner_ip=" + scanner_ip
				+ ", scanner_port=" + scanner_port + "]";
	}
	public CNCScannersInfo(int id, String scanner_model, String scanner_ip, String scanner_port) {
		super();
		this.id = id;
		this.scanner_model = scanner_model;
		this.scanner_ip = scanner_ip;
		this.scanner_port = scanner_port;
	}
	public CNCScannersInfo() {
		super();
	}
	
}
