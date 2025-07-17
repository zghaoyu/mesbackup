package com.cncmes.data;

/**
 * Socket Data Buffer
 * @author LI ZI LONG
 *
 */
public class SocketData {
	private static String data;
	
	public static void setData(String s){
		data = s;
	}
	
	public static String getData(){
		return data;
	}
}
