package com.cncmes.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;

public class NetUtils {
	private NetUtils(){}
	
	public static String[] getOnlineIPs() throws Exception {
		ArrayList<String> ips = new ArrayList<String>();
		
		try {
			String localHost = InetAddress.getLocalHost().toString();
			String[] str = localHost.split("/");
			if(str.length > 1){
				ips.add(str[1].trim());
			}else{
				ips.add(localHost);
			}
			
			Process process = Runtime.getRuntime().exec("arp -a");
			InputStreamReader inputStr = new InputStreamReader(
					process.getInputStream(), "GBK");
			BufferedReader br = new BufferedReader(inputStr);
			String temp = null;
			while ((temp = br.readLine()) != null) {
				str = temp.split("\\w{2}-\\w{2}-\\w{2}-\\w{2}-\\w{2}-\\w{2}");
				if(str.length > 1) ips.add(str[0].trim());
			}
			
			process.destroy();
			br.close();
			inputStr.close();
		} catch (Exception e) {
			throw new Exception("Get online IPs error: "+e.getMessage());
		}
		
		String[] arrIP = null;
		if(!ips.isEmpty()){
			arrIP = new String[ips.size()];
			for(int i=0; i<ips.size(); i++){
				arrIP[i] = ips.get(i);
			}
		}
		
		return arrIP;
	}
}
