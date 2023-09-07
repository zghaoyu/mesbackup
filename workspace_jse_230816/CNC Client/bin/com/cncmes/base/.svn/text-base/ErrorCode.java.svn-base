package com.cncmes.base;

/**
 * System Error Codes
 * @author LI ZI LONG
 *
 */
public enum ErrorCode {
	MC_CONNECT("Machine connection failed"),
	MC_OPEN_DOOR("Door opening failed"),
	MC_CLOSE_DOOR("Door closing failed"),
	MC_CLAMP_FIXTURE("Fixture clamping failed"),
	MC_RELEASE_FIXTURE("Fixture release failed"),
	MC_START_MACHINING("Start machining failed"),
	MC_INTERRUPTION("Machine interruption"),
	MC_NCPROGRAM("NC programs uploading failed"),
	MC_DRIVER("Machine driver loading failed"),
	MC_MAINPROGRAM("NC main program is not activated"),
	RB_CONNECT("Robot fails to connect"),
	RB_MOVETO_RACK("Robot fails moving to rack"),
	RB_MOVETO_MACHINE("Robot fails moving to machine"),
	RB_PICKMTFROM_RACK("Robot fails picking material from rack"),
	RB_PUTMTONTO_RACK("Robot fails putting material onto rack"),
	RB_PICKMTFROM_TRAY("Robot fails picking material from tray"),
	RB_PUTMTONTO_TRAY("Robot fails putting material onto tray"),
	RB_PICKMTFROM_MACHINE("Robot fails picking material from machine"),
	RB_PUTMTONTO_MACHINE("Robot fails putting material onto machine"),
	RB_SCANBARCODE("Robot fails to scan barcode"),
	RB_DRIVER("Robot driver loading failed")
	;
	
	private String errDesc;
	private ErrorCode(String errDesc){
		this.errDesc = errDesc;
	}
	public String getErrDesc(){
		return errDesc;
	}
}
