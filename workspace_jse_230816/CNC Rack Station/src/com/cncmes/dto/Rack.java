package com.cncmes.dto;

public class Rack {
	private int id;
	private String rack_name;
	private int rack_type;
	private int capacity;
	private int line_id;
	private String create_time;
	private int user_id;
	private String ip_address;
	private String pc_name;
	private int is_deleted;
	private String realTableName = "rack";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRack_name() {
		return rack_name;
	}

	public void setRack_name(String rack_name) {
		this.rack_name = rack_name;
	}

	public int getRack_type() {
		return rack_type;
	}

	public void setRack_type(int rack_type) {
		this.rack_type = rack_type;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getLine_id() {
		return line_id;
	}

	public void setLine_id(int line_id) {
		this.line_id = line_id;
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

	public Rack(int id, String rack_name, int rack_type, int capacity, int line_id, String create_time, int user_id,
			String ip_address, String pc_name, int is_deleted, String realTableName) {
		super();
		this.id = id;
		this.rack_name = rack_name;
		this.rack_type = rack_type;
		this.capacity = capacity;
		this.line_id = line_id;
		this.create_time = create_time;
		this.user_id = user_id;
		this.ip_address = ip_address;
		this.pc_name = pc_name;
		this.is_deleted = is_deleted;
		this.realTableName = realTableName;
	}

	public Rack() {
		super();
	}

	@Override
	public String toString() {
		return rack_name;
	}

}
