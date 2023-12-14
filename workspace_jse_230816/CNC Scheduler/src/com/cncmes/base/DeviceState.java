package com.cncmes.base;

import java.awt.Color;

public enum DeviceState {
	STANDBY(new Color(255,255,255)),
	WORKING(new Color(102,255,102)), 
	ALARMING(new Color(255,255,153)), //¾¯±¨
	FINISH(new Color(102,255,255)),
	SHUTDOWN(new Color(255,204,255)),
	WAITUL(new Color(255,102,255)),
	PLAN(new Color(255,102,255)),
	UNSCHEDULE(new Color(255,102,255)),
	HANDLING(new Color(255,102,255)),
	LOCK(new Color(255,102,255)),
	FIXED(new Color(255,102,255));
	
	private Color color;
	private DeviceState(Color color){
		this.color = color;
	}
	public Color getColor(){
		return this.color;
	}
	
}
