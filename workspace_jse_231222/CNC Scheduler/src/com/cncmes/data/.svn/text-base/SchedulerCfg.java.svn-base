package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.RunningData;
import com.cncmes.base.SchedulerItems;

public class SchedulerCfg extends RunningData<SchedulerItems> {
	private static SchedulerCfg schedulerCfg = new SchedulerCfg();
	private SchedulerCfg(){}
	public static SchedulerCfg getInstance(){
		return schedulerCfg;
	}
	
	public int getPort(SchedulerItems portType){
		int port = 0;
		
		if(dataMap.size() > 0){
			for(String ip:dataMap.keySet()){
				LinkedHashMap<SchedulerItems,Object> s = dataMap.get(ip);
				if(null != s){
					if(portType == SchedulerItems.PORTMACHINE) port = Integer.parseInt((String)s.get(SchedulerItems.PORTMACHINE));
					if(portType == SchedulerItems.PORTROBOT) port = Integer.parseInt((String)s.get(SchedulerItems.PORTROBOT));
					if(portType == SchedulerItems.PORTMATERIAL) port = Integer.parseInt((String)s.get(SchedulerItems.PORTMATERIAL));
					if(portType == SchedulerItems.PORTTASK) port = Integer.parseInt((String)s.get(SchedulerItems.PORTTASK));
					if(portType == SchedulerItems.PORTTASKUPDATE) port = Integer.parseInt((String)s.get(SchedulerItems.PORTTASKUPDATE));
					if(portType == SchedulerItems.PORTRACK) port = Integer.parseInt((String)s.get(SchedulerItems.PORTRACK));
				}
			}
		}
		
		return port;
	}
	
	public int[] getAllPorts(){
		int[] ports = null;
		String pts = "";
		if(dataMap.size() > 0){
			for(String ip:dataMap.keySet()){
				LinkedHashMap<SchedulerItems,Object> s = dataMap.get(ip);
				if(null != s){
					pts = (String)s.get(SchedulerItems.PORTMACHINE);
					pts += "," + (String)s.get(SchedulerItems.PORTROBOT);
					pts += "," + (String)s.get(SchedulerItems.PORTMATERIAL);
					pts += "," + (String)s.get(SchedulerItems.PORTTASK);
					pts += "," + (String)s.get(SchedulerItems.PORTTASKUPDATE);
					pts += "," + (String)s.get(SchedulerItems.PORTRACK);
				}
			}
		}
		
		if(!"".equals(pts)){
			String[] port = pts.split(",");
			ports = new int[port.length];
			for(int i=0; i<port.length; i++){
				ports[i] = Integer.parseInt(port[i]);
			}
		}
		
		return ports;
	}
}
