package com.cncmes.dto;

/**
 * the Data Transfer Object for database access
 * 
 * @author W000586 Hui Zhi 2022/1/10
 *
 */
public class CNCNcProgram {
	private int id;
	private String dwgno;
	private String rev;
	private String ip;
	private String pc_name;
	private int user_id;
	private String upload_date;
	private int proc_no;
	private String proc_program;
	private String cnc_model;
	private String file_name;
	private String realTableName = "cnc_ncprogram";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDwgno() {
		return dwgno;
	}
	public void setDwgno(String dwgno) {
		this.dwgno = dwgno;
	}
	public String getRev() {
		return rev;
	}
	public void setRev(String rev) {
		this.rev = rev;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPc_name() {
		return pc_name;
	}
	public void setPc_name(String pc_name) {
		this.pc_name = pc_name;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getUpload_date() {
		return upload_date;
	}
	public void setUpload_date(String upload_date) {
		this.upload_date = upload_date;
	}
	public int getProc_no() {
		return proc_no;
	}
	public void setProc_no(int proc_no) {
		this.proc_no = proc_no;
	}
	public String getProc_program() {
		return proc_program;
	}
	public void setProc_program(String proc_program) {
		this.proc_program = proc_program;
	}
	public String getCnc_model() {
		return cnc_model;
	}
	public void setCnc_model(String cnc_model) {
		this.cnc_model = cnc_model;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	public CNCNcProgram(int id, String dwgno, String rev, String ip, String pc_name, int user_id, String upload_date,
			int proc_no, String proc_program, String cnc_model, String file_name, String realTableName) {
		super();
		this.id = id;
		this.dwgno = dwgno;
		this.rev = rev;
		this.ip = ip;
		this.pc_name = pc_name;
		this.user_id = user_id;
		this.upload_date = upload_date;
		this.proc_no = proc_no;
		this.proc_program = proc_program;
		this.cnc_model = cnc_model;
		this.file_name = file_name;
		this.realTableName = realTableName;
	}
	public CNCNcProgram() {
		super();
	}


}