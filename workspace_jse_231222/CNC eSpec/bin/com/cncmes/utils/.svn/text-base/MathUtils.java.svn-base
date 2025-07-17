package com.cncmes.utils;

import java.math.BigDecimal;
import java.security.MessageDigest;

public class MathUtils {
	public static String MD5Encode(String str){
		String newStr = "";
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(str.getBytes());
			byte[] arrBytes = md5.digest();
			
			int i;
			StringBuffer buf = new StringBuffer("");
			for(int j=0; j<arrBytes.length; j++){
				i = arrBytes[j];
				if (i < 0) i = i + 256;
				if (i < 16) buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			newStr = buf.toString().toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return newStr;
	}
	
	public static float roundFloat(float fVal,int digits){
		float newVal = new BigDecimal(fVal).setScale(digits, BigDecimal.ROUND_HALF_UP).floatValue();
		return newVal;
	}
}
