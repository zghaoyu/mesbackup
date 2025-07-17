package com.cncmes.dto;
/***
 * 
 * @author W000586
 *	2022/4/20
 */
public class CNCLine {
	private int id;
	private String line_name;
	private String description;
	private String create_time;
	private int user_id;
	private String ip_address;
	private String pc_name;
	private int is_deleted;
	private String realTableName = "cnc_line";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLine_name() {
		return line_name;
	}

	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	public String getPc_name() {
		return pc_name;
	}

	public void setPc_name(String pc_name) {
		this.pc_name = pc_name;
	}

	public int getIs_deleted() {
		return is_deleted;
	}

	public void setIs_deleted(int is_deleted) {
		this.is_deleted = is_deleted;
	}

	public String getRealTableName() {
		return realTableName;
	}

	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}

	public CNCLine(int id, String line_name, String description, String create_time, int user_id, String ip_address,
			String pc_name, int is_deleted, String realTableName) {
		super();
		this.id = id;
		this.line_name = line_name;
		this.description = description;
		this.create_time = create_time;
		this.user_id = user_id;
		this.ip_address = ip_address;
		this.pc_name = pc_name;
		this.is_deleted = is_deleted;
		this.realTableName = realTableName;
	}

	public CNCLine() {
		super();
	}

	@Override
	public String toString() {
		return line_name;
	}

}
