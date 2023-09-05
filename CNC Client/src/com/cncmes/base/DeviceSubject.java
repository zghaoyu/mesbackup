package com.cncmes.base;

import java.util.ArrayList;

/**
 * Interface for All Running Data Subjects
 * @author LI ZI LONG
 *
 */
public interface DeviceSubject {
	/**
	 * @param observer the data observer to be registered
	 */
	public void registerObserver(DeviceObserver observer);
	
	/**
	 * @param observer the data observer to be deleted
	 */
	public void deleteObserver(DeviceObserver observer);
	
	/**
	 * @param observers all data observers to notify
	 * @param data the data to be sent to all observers
	 * @param threadMode true to notify all observers in sub thread
	 * @param threadSequential true means all sub threads are sequentially executed
	 * @param theLastThread true if it is the last thread, only useful while threadMode and threadSequential is both true
	 */
	public void notifyObservers(ArrayList<DeviceObserver> observers, String data, boolean threadMode, boolean threadSequential, boolean theLastThread);
}
