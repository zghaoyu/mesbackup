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
	private int machine;
	private int tools;
	private String ip;
	private String pc_name;
	private int user_Id;
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

	public int getMachine() {
		return machine;
	}

	public void setMachine(int machine) {
		this.machine = machine;
	}

	public int getTools() {
		return tools;
	}

	public void setTools(int tools) {
		this.tools = tools;
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

	public int getUser_Id() {
		return user_Id;
	}

	public void setUser_Id(int user_Id) {
		this.user_Id = user_Id;
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

	public CNCNcProgram() {
		super();
	}

	public CNCNcProgram(int id, String dwgno, String rev, int machine, int tools, String ip, String pc_name,
			int user_Id, String upload_date, int proc_no, String proc_program, String cnc_model, String file_name,
			String realTableName) {
		super();
		this.id = id;
		this.dwgno = dwgno;
		this.rev = rev;
		this.machine = machine;
		this.tools = tools;
		this.ip = ip;
		this.pc_name = pc_name;
		this.user_Id = user_Id;
		this.upload_date = upload_date;
		this.proc_no = proc_no;
		this.proc_program = proc_program;
		this.cnc_model = cnc_model;
		this.file_name = file_name;
		this.realTableName = realTableName;
	}

	@Override
	public String toString() {
		return "CNCNcProgram [id=" + id + ", dwgno=" + dwgno + ", rev=" + rev + ", machine=" + machine + ", tools="
				+ tools + ", ip=" + ip + ", pc_name=" + pc_name + ", user_Id=" + user_Id + ", upload_date="
				+ upload_date + ", proc_no=" + proc_no + ", proc_program=" + proc_program + ", cnc_model=" + cnc_model
				+ ", file_name=" + file_name + ", realTableName=" + realTableName + "]";
	}

}