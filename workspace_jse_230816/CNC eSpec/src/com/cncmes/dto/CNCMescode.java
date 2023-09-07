package com.cncmes.dto;

public class CNCMescode {
	private long id;
	private long specid;
	private String mescode;
	private String description;
	private String realTableName = "cnc_mescode";
	public long getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getSpecid() {
		return specid;
	}
	public void setSpecid(int specid) {
		this.specid = specid;
	}
	public String getMescode() {
		return mescode;
	}
	public void setMescode(String mescode) {
		this.mescode = mescode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	public CNCMescode(int id, int specid, String mescode, String description, String realTableName) {
		super();
		this.id = id;
		this.specid = specid;
		this.mescode = mescode;
		this.description = description;
		this.realTableName = realTableName;
	}
	public CNCMescode() {
		super();
	}
	
}
