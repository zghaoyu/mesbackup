package com.cncmes.base;

public enum PermissionItems {
	DBWRITE(1),
	DEVMONITORING(2),
	TASKHANDLING(3),
	RACKMANAGER(4),
	SCHEDULER(5),
	SYSCONFIG(6),
	DEVCONTROLLER(7);
	
	private int val;
	private PermissionItems(int val){
		this.val = val;
	}
	public int getVal(){
		return this.val;
	}
}