package com.cncmes.dto;

/**
 * @author LI ZI LONG
 * the Data Transfer Object for database access
 *
 */
public class CNCRobots {
	private int id;
	private String robot_model;
	private String driver;
	private String cmdendchr;
	private String realTableName = "cnc_robots";
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
		return "CNCRobots [id=" + id + ", robot_model=" + robot_model + ", driver=" + driver+ ", cmdendchr=" + cmdendchr + ", realTableName="
				+ realTableName + "]";
	}
	public CNCRobots() {
		super();
	}
	public CNCRobots(int id, String robot_model, String driver, String cmdendchr, String realTableName) {
		super();
		this.id = id;
		this.robot_model = robot_model;
		this.driver = driver;
		this.cmdendchr = cmdendchr;
		this.realTableName = realTableName;
	}
	
}
