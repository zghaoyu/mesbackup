package com.cncmes.dto;

public class Fixture {
	private int id;
	private int type_id;
	private String fixture_no;
	private String create_time;
	private int is_deleted;
	private int user_id;
	private String ip_address;
	private String pc_name;
	private String realTableName = "fixture";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType_id() {
		return type_id;
	}

	public void setType_id(int type_id) {
		this.type_id = type_id;
	}

	public String getFixture_no() {
		return fixture_no;
	}

	public void setFixture_no(String fixture_no) {
		this.fixture_no = fixture_no;
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

	public Fixture(int id, int type_id, String fixture_no, String create_time, int is_deleted, int user_id,
			String ip_address, String pc_name, String realTableName) {
		super();
		this.id = id;
		this.type_id = type_id;
		this.fixture_no = fixture_no;
		this.create_time = create_time;
		this.is_deleted = is_deleted;
		this.user_id = user_id;
		this.ip_address = ip_address;
		this.pc_name = pc_name;
		this.realTableName = realTableName;
	}

	public Fixture() {
		super();
	}

	@Override
	public String toString() {
		return fixture_no;
	}

}
