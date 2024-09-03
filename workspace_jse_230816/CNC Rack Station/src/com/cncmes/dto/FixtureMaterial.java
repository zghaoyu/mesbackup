package com.cncmes.dto;

/**
 * 
 * @author W000586 Hui Zhi Fang 2022/3/10
 *
 */
public class FixtureMaterial {
	private int id;
	private int material_id;
	private int fixture_id;
	private String scan_time;
	private int user_id;
	private String ip_address;
	private String pc_name;
	private int is_released;
	private int is_deleted;
	private String realTableName = "fixture_material";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMaterial_id() {
		return material_id;
	}

	public void setMaterial_id(int material_id) {
		this.material_id = material_id;
	}

	public int getFixture_id() {
		return fixture_id;
	}

	public void setFixture_id(int fixture_id) {
		this.fixture_id = fixture_id;
	}

	public String getScan_time() {
		return scan_time;
	}

	public void setScan_time(String scan_time) {
		this.scan_time = scan_time;
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

	public int getIs_released() {
		return is_released;
	}

	public void setIs_released(int is_released) {
		this.is_released = is_released;
	}

	public String getRealTableName() {
		return realTableName;
	}

	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}

	public int getIs_deleted() {
		return is_deleted;
	}

	public void setIs_deleted(int is_deleted) {
		this.is_deleted = is_deleted;
	}

	public FixtureMaterial(int id, int material_id, int fixture_id, String scan_time, int user_id, String ip_address,
			String pc_name, int is_released, int is_deleted, String realTableName) {
		super();
		this.id = id;
		this.material_id = material_id;
		this.fixture_id = fixture_id;
		this.scan_time = scan_time;
		this.user_id = user_id;
		this.ip_address = ip_address;
		this.pc_name = pc_name;
		this.is_released = is_released;
		this.is_deleted = is_deleted;
		this.realTableName = realTableName;
	}

	public FixtureMaterial() {
		super();
	}

	@Override
	public String toString() {
		return "FixtureMaterial [id=" + id + ", material_id=" + material_id + ", fixture_id=" + fixture_id
				+ ", scan_time=" + scan_time + ", user_id=" + user_id + ", ip_address=" + ip_address + ", pc_name="
				+ pc_name + ", is_released=" + is_released + ", is_deleted=" + is_deleted + ", realTableName="
				+ realTableName + "]";
	}

}
