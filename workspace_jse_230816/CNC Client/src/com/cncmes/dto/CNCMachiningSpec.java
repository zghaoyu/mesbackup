package com.cncmes.dto;

/**
 * @author LI ZI LONG
 * the Data Transfer Object for database access
 *
 */
public class CNCMachiningSpec {
	private long id;
	private String dwgno;
	private String spec_type;
	private String description;
	private String proc1_name;
	private String proc1_ncmodel;
	private String proc1_program;
	private String proc1_simtime;
	private int proc1_surface;
	
	private String proc2_name;
	private String proc2_ncmodel;
	private String proc2_program;
	private String proc2_simtime;
	private int proc2_surface;
	
	private String proc3_name;
	private String proc3_ncmodel;
	private String proc3_program;
	private String proc3_simtime;
	private int proc3_surface;
	
	private String proc4_name;
	private String proc4_ncmodel;
	private String proc4_program;
	private String proc4_simtime;
	private int proc4_surface;
	
	private String proc5_name;
	private String proc5_ncmodel;
	private String proc5_program;
	private String proc5_simtime;
	private int proc5_surface;
	
	private String proc6_name;
	private String proc6_ncmodel;
	private String proc6_program;
	private String proc6_simtime;
	private int proc6_surface;
	
	private String realTableName = "cnc_machiningspec";

	public long getId() {
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
	
	public String getSpec_type() {
		return spec_type;
	}

	public void setSpec_type(String spec_type) {
		this.spec_type = spec_type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProc1_name() {
		return proc1_name;
	}

	public void setProc1_name(String proc1_name) {
		this.proc1_name = proc1_name;
	}

	public String getProc1_ncmodel() {
		return proc1_ncmodel;
	}

	public void setProc1_ncmodel(String proc1_ncmodel) {
		this.proc1_ncmodel = proc1_ncmodel;
	}

	public String getProc1_program() {
		return proc1_program;
	}

	public void setProc1_program(String proc1_program) {
		this.proc1_program = proc1_program;
	}

	public String getProc1_simtime() {
		return proc1_simtime;
	}

	public void setProc1_simtime(String proc1_simtime) {
		this.proc1_simtime = proc1_simtime;
	}

	public int getProc1_surface() {
		return proc1_surface;
	}

	public void setProc1_surface(int proc1_surface) {
		this.proc1_surface = proc1_surface;
	}

	public String getProc2_name() {
		return proc2_name;
	}

	public void setProc2_name(String proc2_name) {
		this.proc2_name = proc2_name;
	}

	public String getProc2_ncmodel() {
		return proc2_ncmodel;
	}

	public void setProc2_ncmodel(String proc2_ncmodel) {
		this.proc2_ncmodel = proc2_ncmodel;
	}

	public String getProc2_program() {
		return proc2_program;
	}

	public void setProc2_program(String proc2_program) {
		this.proc2_program = proc2_program;
	}

	public String getProc2_simtime() {
		return proc2_simtime;
	}

	public void setProc2_simtime(String proc2_simtime) {
		this.proc2_simtime = proc2_simtime;
	}

	public int getProc2_surface() {
		return proc2_surface;
	}

	public void setProc2_surface(int proc2_surface) {
		this.proc2_surface = proc2_surface;
	}

	public String getProc3_name() {
		return proc3_name;
	}

	public void setProc3_name(String proc3_name) {
		this.proc3_name = proc3_name;
	}

	public String getProc3_ncmodel() {
		return proc3_ncmodel;
	}

	public void setProc3_ncmodel(String proc3_ncmodel) {
		this.proc3_ncmodel = proc3_ncmodel;
	}

	public String getProc3_program() {
		return proc3_program;
	}

	public void setProc3_program(String proc3_program) {
		this.proc3_program = proc3_program;
	}

	public String getProc3_simtime() {
		return proc3_simtime;
	}

	public void setProc3_simtime(String proc3_simtime) {
		this.proc3_simtime = proc3_simtime;
	}

	public int getProc3_surface() {
		return proc3_surface;
	}

	public void setProc3_surface(int proc3_surface) {
		this.proc3_surface = proc3_surface;
	}

	public String getProc4_name() {
		return proc4_name;
	}

	public void setProc4_name(String proc4_name) {
		this.proc4_name = proc4_name;
	}

	public String getProc4_ncmodel() {
		return proc4_ncmodel;
	}

	public void setProc4_ncmodel(String proc4_ncmodel) {
		this.proc4_ncmodel = proc4_ncmodel;
	}

	public String getProc4_program() {
		return proc4_program;
	}

	public void setProc4_program(String proc4_program) {
		this.proc4_program = proc4_program;
	}

	public String getProc4_simtime() {
		return proc4_simtime;
	}

	public void setProc4_simtime(String proc4_simtime) {
		this.proc4_simtime = proc4_simtime;
	}

	public int getProc4_surface() {
		return proc4_surface;
	}

	public void setProc4_surface(int proc4_surface) {
		this.proc4_surface = proc4_surface;
	}

	public String getProc5_name() {
		return proc5_name;
	}

	public void setProc5_name(String proc5_name) {
		this.proc5_name = proc5_name;
	}

	public String getProc5_ncmodel() {
		return proc5_ncmodel;
	}

	public void setProc5_ncmodel(String proc5_ncmodel) {
		this.proc5_ncmodel = proc5_ncmodel;
	}

	public String getProc5_program() {
		return proc5_program;
	}

	public void setProc5_program(String proc5_program) {
		this.proc5_program = proc5_program;
	}

	public String getProc5_simtime() {
		return proc5_simtime;
	}

	public void setProc5_simtime(String proc5_simtime) {
		this.proc5_simtime = proc5_simtime;
	}

	public int getProc5_surface() {
		return proc5_surface;
	}

	public void setProc5_surface(int proc5_surface) {
		this.proc5_surface = proc5_surface;
	}

	public String getProc6_name() {
		return proc6_name;
	}

	public void setProc6_name(String proc6_name) {
		this.proc6_name = proc6_name;
	}

	public String getProc6_ncmodel() {
		return proc6_ncmodel;
	}

	public void setProc6_ncmodel(String proc6_ncmodel) {
		this.proc6_ncmodel = proc6_ncmodel;
	}

	public String getProc6_program() {
		return proc6_program;
	}

	public void setProc6_program(String proc6_program) {
		this.proc6_program = proc6_program;
	}

	public String getProc6_simtime() {
		return proc6_simtime;
	}

	public void setProc6_simtime(String proc6_simtime) {
		this.proc6_simtime = proc6_simtime;
	}

	public int getProc6_surface() {
		return proc6_surface;
	}

	public void setProc6_surface(int proc6_surface) {
		this.proc6_surface = proc6_surface;
	}

	public String getRealTableName() {
		return realTableName;
	}

	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}

	public CNCMachiningSpec(int id, String dwgno, String spec_type, String description, String proc1_name, String proc1_ncmodel,
			String proc1_program, String proc1_simtime, int proc1_surface, String proc2_name, String proc2_ncmodel,
			String proc2_program, String proc2_simtime, int proc2_surface, String proc3_name, String proc3_ncmodel,
			String proc3_program, String proc3_simtime, int proc3_surface, String proc4_name, String proc4_ncmodel,
			String proc4_program, String proc4_simtime, int proc4_surface, String proc5_name, String proc5_ncmodel,
			String proc5_program, String proc5_simtime, int proc5_surface, String proc6_name, String proc6_ncmodel,
			String proc6_program, String proc6_simtime, int proc6_surface, String realTableName) {
		super();
		this.id = id;
		this.dwgno = dwgno;
		this.spec_type = spec_type;
		this.description = description;
		this.proc1_name = proc1_name;
		this.proc1_ncmodel = proc1_ncmodel;
		this.proc1_program = proc1_program;
		this.proc1_simtime = proc1_simtime;
		this.proc1_surface = proc1_surface;
		this.proc2_name = proc2_name;
		this.proc2_ncmodel = proc2_ncmodel;
		this.proc2_program = proc2_program;
		this.proc2_simtime = proc2_simtime;
		this.proc2_surface = proc2_surface;
		this.proc3_name = proc3_name;
		this.proc3_ncmodel = proc3_ncmodel;
		this.proc3_program = proc3_program;
		this.proc3_simtime = proc3_simtime;
		this.proc3_surface = proc3_surface;
		this.proc4_name = proc4_name;
		this.proc4_ncmodel = proc4_ncmodel;
		this.proc4_program = proc4_program;
		this.proc4_simtime = proc4_simtime;
		this.proc4_surface = proc4_surface;
		this.proc5_name = proc5_name;
		this.proc5_ncmodel = proc5_ncmodel;
		this.proc5_program = proc5_program;
		this.proc5_simtime = proc5_simtime;
		this.proc5_surface = proc5_surface;
		this.proc6_name = proc6_name;
		this.proc6_ncmodel = proc6_ncmodel;
		this.proc6_program = proc6_program;
		this.proc6_simtime = proc6_simtime;
		this.proc6_surface = proc6_surface;
		this.realTableName = realTableName;
	}

	public CNCMachiningSpec() {
		super();
	}
	
}
