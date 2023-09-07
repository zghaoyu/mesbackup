package com.cncmes.base;

/**
 * Interface for bar code Scanner
 * @author LI ZI LONG
 *
 */
public interface Scanner {
	/**
	 * @param ip the IP address of the Scanner
	 * @return content of the bar code
	 */
	public String doScanning(String ip);
}
