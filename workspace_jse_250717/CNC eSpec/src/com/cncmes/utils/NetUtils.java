package com.cncmes.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NetUtils {
	private NetUtils(){}
	
	public static String getLocalIP(){
		String localIP = "";
		try {
			localIP = InetAddress.getLocalHost().toString();
			String[] str = localIP.split("/");
			if(str.length > 1) localIP = str[1].trim();
		} catch (UnknownHostException e) {
			LogUtils.errorLog("getLocalIP ERR: "+e.getMessage());
		}
		
		return localIP;
	}
	/**
	 * add by Hui Zhi 2022/1/10
	 * @return
	 */
	public static String getLocalHostName(){
		String hostName = "";
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (UnknownHostException e) {
			LogUtils.errorLog("getLocalHostName ERR: "+e.getMessage());
		}
		return hostName;
	}
	
	/**
	 * Get the network status about the relativeIP
	 * @param relativeIP the interested IP to check
	 * @return network status string about relativeIP
	 */
	public static String getNetworkStatus(String relativeIP){
		String rtn = "", msg = "";
		
		try {
			Process process = Runtime.getRuntime().exec("netstat -ant");
			InputStreamReader inputStr = new InputStreamReader(
					process.getInputStream(), "GBK");
			BufferedReader br = new BufferedReader(inputStr);
			String temp = null;
			while ((temp = br.readLine()) != null) {
				if(temp.contains(relativeIP)){
					if("".equals(msg)){
						msg = temp;
					}else{
						msg += "\r\n" + temp;
					}
				}
			}
			
			process.destroy();
			br.close();
			inputStr.close();
		} catch (IOException e) {
			msg = e.getMessage();
		}
		
		if("".equals(rtn)) rtn = msg;
		return rtn;
	}
	
	/**
	 * @param hostIP the IP address to ping
	 * @return "OK" if successfully ping the host
	 */
	public static String pingHost(String hostIP){
		String rtn = "", msg = "";
		
		try {
			Process process = Runtime.getRuntime().exec("ping "+hostIP+" -n 1");
			InputStreamReader inputStr = new InputStreamReader(
					process.getInputStream(), "GBK");
			BufferedReader br = new BufferedReader(inputStr);
			String temp = null;
			while ((temp = br.readLine()) != null) {
				if(temp.contains("(0%")) rtn = "OK";
				if("".equals(msg)){
					msg = temp;
				}else{
					msg += "\t" + temp;
				}
			}
			
			process.destroy();
			br.close();
			inputStr.close();
		} catch (IOException e) {
			msg = e.getMessage();
		}
		
		if("".equals(rtn)) rtn = msg;
		return rtn;
	}
	
	public static boolean waitUntilNetworkOK(String checkIP, int timeout_minutes, int interval_s){
		boolean ok = false;
		String pingRtn = "";
		
		long loopCnt = timeout_minutes * 60 / interval_s;
		for(long i=0; i<loopCnt; i++){
			pingRtn = pingHost(checkIP);
			if("OK".equals(pingRtn)){
				ok = true;
				break;
			}else{
				LogUtils.debugLog("Ping_", pingRtn);
				try {
					Thread.sleep(interval_s*1000);
				} catch (InterruptedException e) {
				}
			}
		}
		
		return ok;
	}
	
	public static String[] getOnlineIPs() throws Exception {
		String localIP = "", curIP = "", netMask = "";
		ArrayList<String> ips = new ArrayList<String>();
		
		try {
			String localHost = InetAddress.getLocalHost().toString();
			String[] str = localHost.split("/");
			if(str.length > 1){
				localIP = str[1].trim();
				ips.add(localIP);
			}else{
				localIP = localHost;
				ips.add(localHost);
			}
			if(localIP.lastIndexOf('.')>0){
				netMask = localIP.substring(0, localIP.lastIndexOf('.'));
				if(netMask.lastIndexOf('.')>0) netMask = netMask.substring(0, netMask.lastIndexOf('.'));
			}
			
			Process process = Runtime.getRuntime().exec("arp -a");
			InputStreamReader inputStr = new InputStreamReader(
					process.getInputStream(), "GBK");
			BufferedReader br = new BufferedReader(inputStr);
			String temp = null;
			while ((temp = br.readLine()) != null) {
				str = temp.split("\\w{2}-\\w{2}-\\w{2}-\\w{2}-\\w{2}-\\w{2}");
				if(str.length > 1){
					curIP = str[0].trim();
					if(curIP.startsWith(netMask)){
						if(!curIP.endsWith(".1") && !curIP.endsWith(".255")) ips.add(curIP);
					}
				}
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
	
	public static String getSubnetMask(){
		StringBuilder maskStr = null;
		InetAddress ip = null;
        NetworkInterface ni = null;
        try {
            ip = InetAddress.getLocalHost();
            ni = NetworkInterface.getByInetAddress(ip);// 搜索绑定了指定IP地址的网络接口
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<InterfaceAddress> list = ni.getInterfaceAddresses();// 获取此网络接口的全部或部分InterfaceAddresses所组成的列表
        if (list.size() > 0) {
            int mask = list.get(0).getNetworkPrefixLength(); // 子网掩码的二进制1的个数
            maskStr = new StringBuilder();
            int[] maskIp = new int[4];
            for (int i = 0; i < maskIp.length; i++) {
                maskIp[i] = (mask >= 8) ? 255 : (mask > 0 ? (mask & 0xff) : 0);
                mask -= 8;
                maskStr.append(maskIp[i]);
                if (i < maskIp.length - 1) {
                    maskStr.append(".");
                }
            }
        }
        
        return ("" + maskStr);
	}
}
