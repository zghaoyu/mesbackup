package com.sto.base;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base of Running Data Object
 * @author LI ZI LONG
 *
 * @param <E> Running Data Item
 */
public class RunningData<E> {
	protected Map<String,LinkedHashMap<E,Object>> dataMap = new LinkedHashMap<String,LinkedHashMap<E,Object>>();
	protected LinkedHashMap<String,Boolean> specificDataChangeFlag = new LinkedHashMap<String,Boolean>();
	protected boolean dataChangeFlag = false;
	
	public void resetSpecificDataChangeFlag(String key){
		specificDataChangeFlag.put(key, false);
	}
	
	public boolean getSpecificDataChangeFlag(String key){
		return (null!=specificDataChangeFlag.get(key)?specificDataChangeFlag.get(key):false);
	}
	
	public void resetDataChangeFlag(){
		dataChangeFlag = false;
	}
	
	public boolean getDataChangeFlag(){
		return dataChangeFlag;
	}
	
	/**
	 * @return Map<String,LinkedHashMap<E,Object>>
	 */
	public Map<String,LinkedHashMap<E,Object>> getDataMap(){
		return dataMap;
	}
	
	public void clearData(){
		if(!dataMap.isEmpty()){
			dataMap.clear();
			dataChangeFlag = true;
		}
	}
	
	public void removeData(String key){
		dataMap.remove(key);
		dataChangeFlag = true;
	}
	
	public int size(){
		int len = 0;
		
		if(!dataMap.isEmpty()) len = dataMap.size();
		return len;
	}
	
	public void setData(String key,E item,Object val){
		LinkedHashMap<E,Object> data = dataMap.get(key);
		if(null == data){
			data = new LinkedHashMap<E,Object>();
			data.put(item, val);
			dataMap.put(key, data);
			specificDataChangeFlag.put(key, true);
			dataChangeFlag = true;
		}else{
			if(val != data.get(item)){
				specificDataChangeFlag.put(key, true);
				dataChangeFlag = true;
			}
			data.put(item, val);
		}
	}
	
	public Object getItemVal(String key,E item){
		Object val = null;
		
		LinkedHashMap<E,Object> data = dataMap.get(key);
		if(null != data) val = data.get(item);
		
		return val;
	}
	/**
	 * @param key
	 * @return LinkedHashMap<E,Object>
	 */
	public LinkedHashMap<E,Object> getData(String key){
		return dataMap.get(key);
	}
	
	public LinkedHashMap<E,Object> getFirstData(){
		LinkedHashMap<E,Object> data = null;
		for(String key:dataMap.keySet()){
			data = dataMap.get(key);
			break;
		}
		return data;
	}
	
	public LinkedHashMap<E,Object> getLastData(){
		LinkedHashMap<E,Object> data = null;
		for(String key:dataMap.keySet()){
			data = dataMap.get(key);
		}
		return data;
	}
	
	public String getFirstKey(){
		String skey = "";
		for(String key:dataMap.keySet()){
			skey = key;
			break;
		}
		return skey;
	}
	
	public String getLastKey(){
		String skey = "";
		for(String key:dataMap.keySet()){
			skey = key;
		}
		return skey;
	}
}
