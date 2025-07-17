package com.cncmes.dto;

/**
 * 
 * the Data Transfer Object for database access
 * 
 * @author W000586 Hui Zhi 2022/1/19
 *
 */
public class CNCProcessCard {
	private int id;
	private String order_no;
	private String drawing_no;
	private String part_no;
	private int user_id;
	private String ip_address;
	private String pc_name;
	private String create_time;
	private int is_delete;
	private String realTableName = "cnc_processcard";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOrder_no() {
		return order_no;
	}
	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}
	public String getDrawing_no() {
		return drawing_no;
	}
	public void setDrawing_no(String drawing_no) {
		this.drawing_no = drawing_no;
	}
	public String getPart_no() {
		return part_no;
	}
	public void setPart_no(String part_no) {
		this.part_no = part_no;
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
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public int getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(int is_delete) {
		this.is_delete = is_delete;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	public CNCProcessCard(int id, String order_no, String drawing_no, String part_no, int user_id, String ip_address,
                          String pc_name, String create_time, int is_delete, String realTableName) {
		super();
		this.id = id;
		this.order_no = order_no;
		this.drawing_no = drawing_no;
		this.part_no = part_no;
		this.user_id = user_id;
		this.ip_address = ip_address;
		this.pc_name = pc_name;
		this.create_time = create_time;
		this.is_delete = is_delete;
		this.realTableName = realTableName;
	}
	public CNCProcessCard() {
		super();
	}

	@Override
	public String toString() {
		return "CNCProcessCard [id=" + id + ", order_no=" + order_no + ", drawing_no=" + drawing_no + ", part_no="
				+ part_no + ", user_id=" + user_id + ", ip_address=" + ip_address + ", pc_name=" + pc_name
				+ ", create_time=" + create_time + ", is_delete=" + is_delete + ", realTableName=" + realTableName
				+ "]";
	}

}
