package com.cncmes.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
	/**
	 * 
	 * @param currentTime Current date time
	 * @param format example "yyyy-MM-dd HH:mm:ss"
	 * @return Date time string in specified format
	 */
	public static String getStringDate(Date currentTime, String format){
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(currentTime);
	}
	
	/**
	 * 
	 * @param format example "yyyy-MM-dd HH:mm:ss"
	 * @return Current date time string in specified format
	 */
	public static String getCurrentDate(String format){
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}
	/**
	 *
	 * @param beginDate
	 * @param currentDate
	 * @return different min of two date
	 */
	public static Long getTwoDatePoor(Date beginDate,Date currentDate)
	{
		long nd = 1000 * 24 * 60 * 60;
		long nh = 1000 * 60 * 60;
		long nm = 1000 * 60;
		long ns = 1000;
		long diff = currentDate.getTime() - beginDate.getTime();
		long min = diff/nm;
		return min;
	}


}
