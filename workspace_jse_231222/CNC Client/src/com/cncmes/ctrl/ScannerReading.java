package com.cncmes.ctrl;

import com.cncmes.base.BarcodeValidate;
import com.cncmes.utils.MathUtils;

/**
 * Get Reading of the Scanner
 * @author LI ZI LONG
 *
 */
public class ScannerReading implements BarcodeValidate {
	private String realBarcode;
	private String rawBarcode;
	
	@Override
	/**
	 * @param barcode The barcode reading
	 * @return The scanner reading(includes checksum)
	 */
	public String doValidate(String barcode) {
		realBarcode = null;
		rawBarcode = barcode;
		System.out.println(barcode.length());
		if(null != barcode && barcode.length() > 32){
			String s1 = barcode.substring(0, barcode.length()-32);
			String s2 = barcode.substring(barcode.length()-32); //The last 32bits is the checksum
			if(s2.equals(MathUtils.MD5Encode(s1))) realBarcode = s1;
		}
		return barcode;
	}
	
	public String getRealBarcode() {
		return realBarcode;
	}
	
	public String getRawBarcode() {
		return rawBarcode;
	}
}
