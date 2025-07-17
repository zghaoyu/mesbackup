package com.cncmes.dto;
/**
 * Task Information
 * @author W000586 Hui Zhi  2022/7/5
 *
 */
public class CNCTask {
	private int id;
	private String task_info;
	private String create_time;
	private String task_id;
	private String line_name;
	private String realTableName = "cnc_task";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTask_info() {
		return task_info;
	}
	public void setTask_info(String task_info) {
		this.task_info = task_info;
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public String getTask_id() {
		return task_id;
	}
	public void setTask_id(String task_id) {
		this.task_id = task_id;
	}
	public String getLine_name() {
		return line_name;
	}
	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	public CNCTask(int id, String task_info, String create_time, String task_id, String line_name,
			String realTableName) {
		super();
		this.id = id;
		this.task_info = task_info;
		this.create_time = create_time;
		this.task_id = task_id;
		this.line_name = line_name;
		this.realTableName = realTableName;
	}
	public CNCTask() {
		super();
	}
	@Override
	public String toString() {
		return "CNCTask [id=" + id + ", task_info=" + task_info + ", create_time=" + create_time + ", task_id="
				+ task_id + ", line_name=" + line_name + ", realTableName=" + realTableName + "]";
	}
	
	
	
}
