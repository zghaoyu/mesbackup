package com.cncmes.dto;

public class CNCMaterial {
	private int id;
	private String material_no;
	private int processcard_id;
	private int user_id;
	private String ip_address;
	private String pc_name;
	private String create_time;
	private int is_delete;
	private int each_material_productnum;
	private String realTableName = "cnc_material";

	public int getEachMaterialProductNum() {
		return each_material_productnum;
	}

	public void setEachMaterialProductNum(int eachMaterialProductNum) {
		this.each_material_productnum = eachMaterialProductNum;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMaterial_no() {
		return material_no;
	}
	public void setMaterial_no(String material_no) {
		this.material_no = material_no;
	}
	public int getProcesscard_id() {
		return processcard_id;
	}
	public void setProcesscard_id(int processcard_id) {
		this.processcard_id = processcard_id;
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
	public CNCMaterial(int id, String material_no, int processcard_id, int user_id, String ip_address, String pc_name,
			String create_time, int is_delete, String realTableName) {
		super();
		this.id = id;
		this.material_no = material_no;
		this.processcard_id = processcard_id;
		this.user_id = user_id;
		this.ip_address = ip_address;
		this.pc_name = pc_name;
		this.create_time = create_time;
		this.is_delete = is_delete;
		this.realTableName = realTableName;
	}
	public CNCMaterial() {
		super();
	}

	public CNCMaterial(int id, String material_no, int processcard_id, int user_id, String ip_address, String pc_name, String create_time, int is_delete, int eachMaterialProductNum, String realTableName) {
		this.id = id;
		this.material_no = material_no;
		this.processcard_id = processcard_id;
		this.user_id = user_id;
		this.ip_address = ip_address;
		this.pc_name = pc_name;
		this.create_time = create_time;
		this.is_delete = is_delete;
		this.each_material_productnum = eachMaterialProductNum;
		this.realTableName = realTableName;
	}

	@Override
	public String toString() {
		return "CNCMaterial{" +
				"id=" + id +
				", material_no='" + material_no + '\'' +
				", processcard_id=" + processcard_id +
				", user_id=" + user_id +
				", ip_address='" + ip_address + '\'' +
				", pc_name='" + pc_name + '\'' +
				", create_time='" + create_time + '\'' +
				", is_delete=" + is_delete +
				", eachMaterialProductNum=" + each_material_productnum +
				", realTableName='" + realTableName + '\'' +
				'}';
	}
}
