package com.cncmes.dto;

/**
 * @author LI ZI LONG
 * the Data Transfer Object for database access
 *
 */
public class CNCUsers {
	private int id;
	private String user_name;
	private String user_pwd;
	private String user_authorities;
	private String user_workid;
	private String user_expirydate;
	private String realTableName = "cnc_users";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getUser_pwd() {
		return user_pwd;
	}
	public void setUser_pwd(String user_pwd) {
		this.user_pwd = user_pwd;
	}
	public String getUser_authority() {
		return user_authorities;
	}
	public void setUser_authority(String user_authority) {
		this.user_authorities = user_authority;
	}
	public String getUser_workid() {
		return user_workid;
	}
	public void setUser_workid(String user_workid) {
		this.user_workid = user_workid;
	}
	public String getUser_expirydate() {
		return user_expirydate;
	}
	public void setUser_expirydate(String user_expirydate) {
		this.user_expirydate = user_expirydate;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	public CNCUsers(int id, String user_name, String user_pwd, String user_authority, String user_workid,
                    String user_expirydate, String realTableName) {
		super();
		this.id = id;
		this.user_name = user_name;
		this.user_pwd = user_pwd;
		this.user_authorities = user_authority;
		this.user_workid = user_workid;
		this.user_expirydate = user_expirydate;
		this.realTableName = realTableName;
	}
	public CNCUsers() {
		super();
	}
	@Override
	public String toString() {
		return "CNCUsers [id=" + id + ", user_name=" + user_name + ", user_pwd=" + user_pwd + ", user_authority="
				+ user_authorities + ", user_workid=" + user_workid + ", user_expirydate=" + user_expirydate
				+ ", realTableName=" + realTableName + "]";
	}
}
