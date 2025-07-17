package com.cncmes.utils;

public class SystemInfo {
	private SystemInfo(){}
	
	private static long totalWorkpieceQty;
	private static long finishedWorkpieceQty;
	
	static{
		totalWorkpieceQty = 0;
		finishedWorkpieceQty = 0;
	}
	
	public static long getTotalWorkpieceQty(){
		return totalWorkpieceQty;
	}
	
	public static long getFinishedWorkpieceQty(){
		return finishedWorkpieceQty;
	}
	
	public static void addTotalWorkpieceQty(long addingQty){
		totalWorkpieceQty += addingQty;
	}
	
	public static void addFinishedWorkpieceQty(long addingQty){
		finishedWorkpieceQty += addingQty;
	}
}
