package com.cncmes.dto;

/**
 * @author LI ZI LONG
 * the Data Transfer Object for database access
 *
 */
public class CNCRobotsInfo {
	private int id;
	private String robot_model;
	private String robot_ip;
	private String robot_port;
	private String scanner_ip;
	private String linename;
	private String tagname;
	private String realTableName = "cnc_robotsinfo";
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
	public String getRobot_model() {
		return robot_model;
	}
	public void setRobot_model(String robot_model) {
		this.robot_model = robot_model;
	}
	public String getRobot_ip() {
		return robot_ip;
	}
	public void setRobot_ip(String robot_ip) {
		this.robot_ip = robot_ip;
	}
	public String getRobot_port() {
		return robot_port;
	}
	public void setRobot_port(String robot_port) {
		this.robot_port = robot_port;
	}
	public String getScanner_ip() {
		return scanner_ip;
	}
	public void setScanner_ip(String scanner_ip) {
		this.scanner_ip = scanner_ip;
	}
	public String getLinename() {
		return linename;
	}
	public void setLinename(String linename) {
		this.linename = linename;
	}
	public String getTagname() {
		return tagname;
	}
	public void setTagname(String tagname) {
		this.tagname = tagname;
	}
	@Override
	public String toString() {
		return "CNCRobotsInfo [id=" + id + ", robot_model=" + robot_model + ", robot_ip=" + robot_ip + ", robot_port="
				+ robot_port + ", scanner_ip=" + scanner_ip + ", linename=" + linename + ", tagname=" + tagname + "]";
	}
	public CNCRobotsInfo(int id, String robot_model, String robot_ip, String robot_port, String scanner_ip, String linename, String tagname) {
		super();
		this.id = id;
		this.robot_model = robot_model;
		this.robot_ip = robot_ip;
		this.robot_port = robot_port;
		this.scanner_ip = scanner_ip;
		this.linename = linename;
		this.tagname = tagname;
	}
	public CNCRobotsInfo() {
		super();
	}
	
}
