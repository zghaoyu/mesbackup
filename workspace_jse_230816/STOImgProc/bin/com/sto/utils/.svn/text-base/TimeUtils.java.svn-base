package com.sto.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
	 * @param daysFromToday Negative value to get the passed time
	 * @return Date of query time
	 */
	public static Date getTime(int daysFromToday){
		Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + daysFromToday);
        return calendar.getTime();
	}
	
	public static Date getTime(long secondsFromToday){
		Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + (int)secondsFromToday);
        return calendar.getTime();
	}
	
	public static long getTimeMillisByNow(double offset_minutes){
		long timeMillis = System.currentTimeMillis();
		
		timeMillis = timeMillis + (long)(offset_minutes*60000);
		
		return timeMillis;
	}
	
	public static int getDaysOffsetTillNow(long offsetTimeMillis){
		long tDiff = offsetTimeMillis - System.currentTimeMillis();
		int daysOffset = (int)(tDiff/1000/3600/24);
		return daysOffset;
	}
	
	public static long getSecondsOffsetTillNow(long offsetTimeMillis){
		long tDiff = (offsetTimeMillis - System.currentTimeMillis())/1000;
		return tDiff;
	}
}
