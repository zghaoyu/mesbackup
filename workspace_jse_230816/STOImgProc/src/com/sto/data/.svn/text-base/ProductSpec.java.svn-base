package com.sto.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sto.base.RunningData;
import com.sto.utils.XmlUtils;

/**
 * Criteria Setting of OK Product
 * @author LI ZI LONG
 *
 */
public class ProductSpec extends RunningData<String> {
	private static ProductSpec productSpec = new ProductSpec();
	private ProductSpec(){}
	static {
		XmlUtils.parseProductSpec();
	}
	
	public static ProductSpec getInstance(){
		return productSpec;
	}
	
	public String[] getSpecNames(){
		String[] specNames = null;
		String spNames = "", sTemp = "";
		
		for(String key:dataMap.keySet()){
			sTemp = (String) dataMap.get(key).get("specName");
			if(null!=sTemp){
				if("".equals(spNames)){
					spNames = sTemp;
				}else{
					spNames += "," + sTemp;
				}
			}
		}
		if(!"".equals(spNames)) specNames = spNames.split(",");
		
		return specNames;
	}
	
	public String getSpecName(String configID){
		String specName = "";
		
		if(null!=dataMap.get(configID)){
			specName = (String) dataMap.get(configID).get("specName");
		}
		
		return specName;
	}
	
	public String[] getConfigIDs(){
		String[] configIDs = null;
		String cfgIDs = "";
		
		for(String key:dataMap.keySet()){
			if("".equals(cfgIDs)){
				cfgIDs = key;
			}else{
				cfgIDs += "," + key;
			}
		}
		if(!"".equals(cfgIDs)) configIDs = cfgIDs.split(",");
		
		return configIDs;
	}
	
	public String getConfigID(String specName){
		String configID = "";
		for(String key:dataMap.keySet()){
			if(null==specName || "".equals(specName)){
				configID = key;
				break;
			}else{
				String sName = (String) dataMap.get(key).get("specName");
				if(null!=sName && sName.trim().equals(specName.trim())){
					configID = key;
					break;
				}
			}
		}
		return configID;
	}
	
	@SuppressWarnings("unchecked")
	public String[] getRawImgFolders(String configID){
		String[] sourceDirs = null;
		String sDirs = "";
		
		LinkedHashMap<String, Object> oriCfg = dataMap.get(configID);
		if(null!=oriCfg && oriCfg.size()>0){
			ArrayList<String> imgPath = (ArrayList<String>) oriCfg.get("rawImgFolders");
			if(null!=imgPath && imgPath.size()>0){
				for(int i=0; i<imgPath.size(); i++){
					if("".equals(sDirs)){
						sDirs = imgPath.get(i);
					}else{
						sDirs += "," + imgPath.get(i);
					}
				}
			}
		}
		if(!"".equals(sDirs)) sourceDirs = sDirs.split(",");
		
		return sourceDirs;
	}
	
	public String[] getRawImgSuffix(String configID){
		String[] suffix = new String[]{"","",""};
		
		LinkedHashMap<String, Object> oriCfg = dataMap.get(configID);
		if(null==oriCfg) oriCfg = dataMap.get("Debug Spec");
		if(null!=oriCfg && oriCfg.size()>0){
			suffix[0] = (String) oriCfg.get("rawImgSuffix");
			suffix[1] = (String) oriCfg.get("okImgSuffix");
			suffix[2] = (String) oriCfg.get("ngImgSuffix");
		}
		
		return suffix;
	}
	
	public Object[][] getImgProcDirs(){
		String[] configIDs = getConfigIDs();
		Object[][] data = null;
		if(null==configIDs){
			data = new Object[][]{};
		}else{
			data = new Object[configIDs.length][2];
			for(int i=0; i<configIDs.length; i++){
				data[i][0] = "imgProcRootDir_"+i;
				data[i][1] = configIDs[i];
			}
		}
		
		return data;
	}
	
	@SuppressWarnings("unchecked")
	public Object[][] getCfgData(String configID, boolean getRawImgPath){
		if(null==configID || "".equals(configID)) configID = getConfigID(null);
		Object[][] data = new Object[][]{};
		
		LinkedHashMap<String, Object> oriCfg = dataMap.get(configID);
		if(null!=oriCfg && oriCfg.size()>0){
			LinkedHashMap<String, Object> cfg = new LinkedHashMap<String, Object>();
			if(getRawImgPath){
				for(String key:oriCfg.keySet()){
					if("rawImgFolders".equals(key)){
						ArrayList<String> imgPath = (ArrayList<String>) oriCfg.get(key);
						if(null!=imgPath && imgPath.size()>0){
							for(int i=0; i<imgPath.size(); i++){
								cfg.put("imgFolder_"+i, imgPath.get(i));
							}
						}
						break;
					}
				}
			}else{
				for(String key:oriCfg.keySet()){
					if("rawImgFolders".equals(key)) continue;
					cfg.put(key, oriCfg.get(key));
				}
			}
			
			int i = -1;
			if(cfg.size() > 0){
				data = new Object[cfg.size()][2];
				for(String key:cfg.keySet()){
					i++;
					data[i][1] = cfg.get(key);
					if("".equals(""+data[i][1])) data[i][1] = "null";
					if("onePixel".equals(key)) key += "(mm)";
					if("minPoleDistance".equals(key)) key += "(mm)";
					if("maxPoleDistance".equals(key)) key += "(mm)";
					if("maxPoleAngle".equals(key)) key += "(degree)";
					data[i][0] = key;
				}
			}
		}
		
		return data;
	}
	
	public String[] getCfgDataTitle(){
		return new String[]{"ParaName","ParaVal"};
	}
}
