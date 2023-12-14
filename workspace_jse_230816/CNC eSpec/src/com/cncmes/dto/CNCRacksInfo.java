package com.cncmes.dto;

public class CNCRacksInfo {
	private int id;
	private int capacity;
	private String linename;
	private int racktype;
	private String ip;
	private int port;
	private String tagname;
	private String realTableName = "cnc_racksinfo";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public String getLinename() {
		return linename;
	}
	public void setLinename(String linename) {
		this.linename = linename;
	}
	public int getRacktype() {
		return racktype;
	}
	public void setRacktype(int racktype) {
		this.racktype = racktype;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getTagname() {
		return tagname;
	}
	public void setTagname(String tagname) {
		this.tagname = tagname;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	public CNCRacksInfo(int id, int capacity, String linename, int racktype, String ip, int port, String tagname, String realTableName) {
		super();
		this.id = id;
		this.capacity = capacity;
		this.linename = linename;
		this.racktype = racktype;
		this.ip = ip;
		this.port = port;
		this.tagname = tagname;
		this.realTableName = realTableName;
	}
	public CNCRacksInfo() {
		super();
	}
	
}
