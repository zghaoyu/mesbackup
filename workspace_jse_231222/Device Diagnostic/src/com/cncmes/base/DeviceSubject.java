package com.cncmes.base;

import java.util.ArrayList;

public interface DeviceSubject {
	public void registerObserver(DeviceObserver observer);
	public void deleteObserver(DeviceObserver observer);
	public void notifyObservers(ArrayList<DeviceObserver> observers, String data, boolean threadMode, boolean threadSequential, boolean theLastThread);
}
