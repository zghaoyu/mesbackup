package com.cncmes.dto;

/**
 * @author LI ZI LONG
 * the Data Transfer Object for database access
 *
 */
public class CNCModels {
	private int id;
	private String cnc_model;
	private String machiningprocess;
	private String driver;
	private String cmdendchr;
	private String datahandler;
	private String realTableName = "cnc_models";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCnc_model() {
		return cnc_model;
	}
	public void setCnc_model(String cnc_model) {
		this.cnc_model = cnc_model;
	}
	public String getMachiningprocess() {
		return machiningprocess;
	}
	public void setMachiningprocess(String machiningprocess) {
		this.machiningprocess = machiningprocess;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public String getCmdendchr() {
		return cmdendchr;
	}
	public void setCmdendchr(String cmdendchr) {
		this.cmdendchr = cmdendchr;
	}
	public String getDatahandler() {
		return datahandler;
	}
	public void setDatahandler(String datahandler) {
		this.datahandler = datahandler;
	}
	public String getRealTableName() {
		return realTableName;
	}
	public void setRealTableName(String realTableName) {
		this.realTableName = realTableName;
	}
	public CNCModels(int id, String cnc_model, String machiningprocess, String driver, String cmdendchr,
			String datahandler, String realTableName) {
		super();
		this.id = id;
		this.cnc_model = cnc_model;
		this.machiningprocess = machiningprocess;
		this.driver = driver;
		this.cmdendchr = cmdendchr;
		this.datahandler = datahandler;
		this.realTableName = realTableName;
	}
	public CNCModels() {
		super();
	}
	@Override
	public String toString() {
		return "CNCModels [id=" + id + ", cnc_model=" + cnc_model + ", machiningprocess=" + machiningprocess
				+ ", driver=" + driver + ", cmdendchr=" + cmdendchr + ", datahandler=" + datahandler
				+ ", realTableName=" + realTableName + "]";
	}
}
