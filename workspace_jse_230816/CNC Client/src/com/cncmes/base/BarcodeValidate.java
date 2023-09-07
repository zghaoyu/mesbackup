package com.cncmes.base;

/**
 * Interface for bar code validation
 * @author LI ZI LONG
 *
 */
public interface BarcodeValidate {
	/**
	 * @param barcode the bar code to be validated
	 * @return content of the real bar code
	 */
	public String doValidate(String barcode);
}
