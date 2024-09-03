package com.cncmes.data;

import com.cncmes.base.MescodeItems;
import com.cncmes.base.RunningData;

public class Mescode extends RunningData<MescodeItems> {
	private static Mescode mescode = new Mescode();
	private Mescode(){}
	public static Mescode getInstance(){
		return mescode;
	}
	
	public String[] getMesCodes(){
		String mcodes = "";
		
		if(dataMap.size() > 0){
			for(String mcode:dataMap.keySet()){
				if("".equals(mcodes)){
					mcodes = mcode;
				}else{
					mcodes += "," + mcode;
				}
			}
		}
		
		return mcodes.split(",");
	}
}
