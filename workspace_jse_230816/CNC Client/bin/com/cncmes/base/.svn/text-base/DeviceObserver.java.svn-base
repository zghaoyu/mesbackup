package com.cncmes.base;

/**
 * Interface for Observer of Running Data
 * @author LI ZI LONG
 *
 */
public interface DeviceObserver {
	/**
	 * @param subject the subject to be observed
	 * @param data the data of the subject
	 * @param threadMode true means notifying the observer in sub thread
	 * @param threadSequential true means the sub thread is sequentially executed 
	 * @param theLastThread true means the last thread, only useful while threadMode and threadSequential is both true
	 */
	public void update(DeviceSubject subject, String data, boolean threadMode, boolean threadSequential, boolean theLastThread);
}
