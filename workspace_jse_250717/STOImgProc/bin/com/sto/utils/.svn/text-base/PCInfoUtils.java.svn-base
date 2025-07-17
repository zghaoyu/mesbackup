package com.sto.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class PCInfoUtils {
	public static boolean licenceOK(){
		boolean ok = false;
		
		LinkedHashMap<String,String> lic = XmlUtils.parseLicFile();
		if(lic.size()>0){
			String mcd = lic.get("MCD");
			String mck = lic.get("MCK");
			if(null!=mcd && null!=mck && !"".equals(mcd) && !"".equals(mck)){
				try {
					mck = DesUtils.decrypt(mck);
					mcd = DesUtils.decrypt(mcd, mck);
					long today = Long.parseLong(TimeUtils.getCurrentDate("yyyyMMdd"));
					long expiry = Long.parseLong(mcd);
					if(expiry > today){
						String mc1 = lic.get("MC");
						String mc2 = "";
						if(null!=mc1 && !"".equals(mc1)){
							mc2 = MathUtils.MD5Encode(PCInfoUtils.getHardDiskSN("C")+PCInfoUtils.getCpuID());
						}
						if(mc2.equals(mc1)) ok = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ok;
	}
	
	public static String getCpuID() {
		String serial = "";
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"wmic", "cpu", "get", "ProcessorId"});
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream());
            String property = sc.next();
            serial = sc.next()+property;
            sc.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return serial;
    }
	
	public static String getMacAddress() {
		String macStr = "0123456789AB";
        try {
            InetAddress ia = InetAddress.getLocalHost();
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            if(null!=mac){
	            StringBuilder sb = new StringBuilder();
	            for (int i = 0; i < mac.length; i++) {
	                if (i != 0) {
	                    sb.append("-");
	                }
	                String s = Integer.toHexString(mac[i] & 0xFF);
	                sb.append(s.length() == 1 ? 0 + s : s);
	            }
	            macStr = sb.toString().toUpperCase();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return macStr;
    }
	
	public static String getHardDiskSN(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n"
                    + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber"; // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.trim();
    }
	
	public static String getMotherboardSN() {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n"
                    + "   (\"Select * from Win32_BaseBoard\") \n"
                    + "For Each objItem in colItems \n"
                    + "    Wscript.Echo objItem.SerialNumber \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec(
                    "cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.trim();
    }
	
	public static String getOSName() {
		return System.getProperty("os.name").toLowerCase();
	}
}
