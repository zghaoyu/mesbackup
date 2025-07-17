package com.cncmes.dto;

public class FixtureType {
	private int id;
	private String fixture_type;
	private String create_time;
	private int is_deleted;
	private String description;
	private int user_id;
	private String ip_address;
	private String pc_name;
	private String realTableName = "fixture_type";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFixture_type() {
		return fixture_type;
	}

	public void setFixture_type(String fixture_type) {
		this.fixture_type = fixture_type;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public int getIs_deleted() {
		return is_deleted;
	}

	public void setIs_deleted(int is_deleted) {
		this.is_deleted = is_deleted;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getRealTableName() {
		return realTableName;
	}

	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}

	public FixtureType(int id, String fixture_type, String create_time, int is_deleted, String description, int user_id,
			String ip_address, String pc_name, String realTableName) {
		super();
		this.id = id;
		this.fixture_type = fixture_type;
		this.create_time = create_time;
		this.is_deleted = is_deleted;
		this.description = description;
		this.user_id = user_id;
		this.ip_address = ip_address;
		this.pc_name = pc_name;
		this.realTableName = realTableName;
	}

	public FixtureType() {
		super();
	}

	@Override
	public String toString() {
		return fixture_type;
	}



}
