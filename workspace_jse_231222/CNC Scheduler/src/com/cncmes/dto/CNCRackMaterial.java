package com.cncmes.dto;
/**
 * 
 * @author W000586 ≤‚ ‘ 2022/7/6
 *
 */
public class CNCRackMaterial {
	private int id;
	private String material_id;
	private String material_info;
	private String create_time;
	private String realTableName = "rack_material";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMaterial_id() {
		return material_id;
	}
	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}
	public String getMaterial_info() {
		return material_info;
	}
	public void setMaterial_info(String material_info) {
		this.material_info = material_info;
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	public CNCRackMaterial(int id, String material_id, String material_info, String create_time, String realTableName) {
		super();
		this.id = id;
		this.material_id = material_id;
		this.material_info = material_info;
		this.create_time = create_time;
		this.realTableName = realTableName;
	}
	public CNCRackMaterial() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "CNCRackMaterial [id=" + id + ", material_id=" + material_id + ", material_info=" + material_info
				+ ", create_time=" + create_time + ", realTableName=" + realTableName + "]";
	}

}
