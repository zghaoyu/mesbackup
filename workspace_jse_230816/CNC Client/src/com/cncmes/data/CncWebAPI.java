package com.cncmes.data;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.RunningData;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.XmlUtils;

import net.sf.json.JSONObject;

/**
 * CNC Web API Configuration
 * @author LI ZI LONG
 *
 */
public class CncWebAPI extends RunningData<String> {
	private static final int MAXINPUTPARAS = 10;
	private static final int MAXOUTPUTPARAS = 10;
	private static final String PARASEPERATOR = ";";
	
	private static CncWebAPI cncWebAPI = new CncWebAPI();
	private CncWebAPI(){}
	static{
		try {
			XmlUtils.parseCncWebAPIXml();
		} catch (Exception e) {
			LogUtils.errorLog("CncWebAPI parses web-api config ERR:"+e.getMessage());
		}
	}
	public static CncWebAPI getInstance(){
		return cncWebAPI;
	}
	
	public String getMainKey(String brand, String model){
		return brand + "_" + model;
	}
	
	public String[] getCommonParasTableTitle(){
		String[] title = new String[]{"ParaName","ParaVal"};
		return title;
	}
	
	public Object[][] getCommonParasTableData(String mainKey){
		Object[][] data = null;
		
		LinkedHashMap<CncWebAPIItems,String> commonCfg = getCommonCfg(mainKey);
		if(null != commonCfg && commonCfg.size() > 0){
			data = new Object[commonCfg.size()][2];
			int i = -1;
			for(CncWebAPIItems item:commonCfg.keySet()){
				i = i + 1;
				data[i][1] = commonCfg.get(item);
				switch(item.name()){
				case "URL":
					data[i][0] = "commandUrl";
					break;
				case "BRAND":
					data[i][0] = "brand";
					break;
				case "MODEL":
					data[i][0] = "model";
					break;
				case "FTPPORT":
					data[i][0] = "ftpPort";
					break;
				case "FTPUSER":
					data[i][0] = "ftpUser";
					break;
				case "FTPPWD":
					data[i][0] = "ftpPwd";
					break;
				case "NCPROGSUB1":
					data[i][0] = "ncProgramName_sub1";
					break;
				case "NCPROGSUB2":
					data[i][0] = "ncProgramName_sub2";
					break;
				case "NCPROGSUB3":
					data[i][0] = "ncProgramName_sub3";
					break;
				case "NCPROGSUB4":
					data[i][0] = "ncProgramName_sub4";
					break;
				case "NCPROGSUB5":
					data[i][0] = "ncProgramName_sub5";
					break;
				case "NCPROGSUB6":
					data[i][0] = "ncProgramName_sub6";
					break;
				case "NCPROGMAIN":
					data[i][0] = "ncProgramName_main";
					break;
				case "COORDINATE1":
					data[i][0] = "cmdCoordinate1";
					break;
				case "COORDINATE2":
					data[i][0] = "cmdCoordinate2";
					break;
				case "COORDINATE3":
					data[i][0] = "cmdCoordinate3";
					break;
				case "COORDINATE4":
					data[i][0] = "cmdCoordinate4";
					break;
				case "COORDINATE5":
					data[i][0] = "cmdCoordinate5";
					break;
				case "COORDINATE6":
					data[i][0] = "cmdCoordinate6";
					break;
				case "DEBUGIP":
					data[i][0] = "debugIP";
					break;
				case "DEBUGPORT":
					data[i][0] = "debugPort";
					break;
				case "sensorcheck":
					data[i][0] = "sensorcheck";
				default:
					data[i][0] = item.name();
					break;
				}
			}
		}
		
		return data;
	}
	
	public String[] getCmdParasTableTitle(){
		String[] title = new String[]{"Index","ParaName","ParaType","ParaVal"};
		return title;
	}
	
	public Object[][] getCmdInputParasTableData(String mainKey, String operationName, String cmdName){
		Object[][] data = null;
		
		LinkedHashMap<CncWebAPIItems,String> cmdInfo = getCmdInfo(mainKey, operationName, cmdName);
		if(null != cmdInfo){
			String[] paras = (null!=cmdInfo.get(CncWebAPIItems.INPARAS))?cmdInfo.get(CncWebAPIItems.INPARAS).split(PARASEPERATOR):null;
			String[] parasType = (null!=cmdInfo.get(CncWebAPIItems.INPARASTYPE))?cmdInfo.get(CncWebAPIItems.INPARASTYPE).split(PARASEPERATOR):null;
			String[] parasVal = (null!=cmdInfo.get(CncWebAPIItems.INPARASVAL))?cmdInfo.get(CncWebAPIItems.INPARASVAL).split(PARASEPERATOR):null;
			
			if(null != paras){
				int rowCnt = paras.length;
				String jsonParas = "", jsParaName = "";
				String allParas = "", allParasType = "", allParasVal = "";
				
				for(int row=0; row<rowCnt; row++){
					if("Json".equals(parasType[row])){
						jsonParas = parasVal[row];
					}else{
						if("".equals(allParas)){
							allParas = paras[row];
							allParasType = parasType[row];
							allParasVal = parasVal[row];
						}else{
							allParas += PARASEPERATOR + paras[row];
							allParasType += PARASEPERATOR + parasType[row];
							allParasVal += PARASEPERATOR + parasVal[row];
						}
					}
				}
				if(!"".equals(jsonParas)){
					JSONObject jsonObj = JSONObject.fromObject(jsonParas);
					@SuppressWarnings("unchecked")
					Iterator<String> iter = jsonObj.keys();
					while(iter.hasNext()){
						jsParaName = iter.next();
						if("".equals(allParas)){
							allParas = jsParaName;
							allParasType = "Json";
							allParasVal = jsonObj.getString(jsParaName);
						}else{
							allParas += PARASEPERATOR + jsParaName;
							allParasType += PARASEPERATOR + "Json";
							allParasVal += PARASEPERATOR + jsonObj.getString(jsParaName);
						}
					}
					
					paras = allParas.split(PARASEPERATOR);
					parasType = allParasType.split(PARASEPERATOR);
					parasVal = allParasVal.split(PARASEPERATOR);
					rowCnt = paras.length;
				}
				
				if(rowCnt < MAXINPUTPARAS){
					data = new Object[MAXINPUTPARAS][4];
				}else{
					data = new Object[rowCnt][4];
				}
				for(int row=0; row<rowCnt; row++){
					data[row][0] = row+1;
					data[row][1] = paras[row];
					data[row][2] = parasType[row];
					data[row][3] = parasVal[row];
				}
				if(rowCnt < MAXINPUTPARAS){
					for(int row=rowCnt; row<MAXINPUTPARAS; row++){
						data[row][0] = row+1;
						data[row][1] = "";
						data[row][2] = "";
						data[row][3] = "";
					}
				}
			}
		}
		
		if(null == data){
			data = new Object[MAXINPUTPARAS][4];
			for(int row=0; row<MAXINPUTPARAS; row++){
				data[row][0] = row+1;
				data[row][1] = "";
				data[row][2] = "";
				data[row][3] = "";
			}
		}
		
		return data;
	}
	
	public Object[][] getCmdOutputParasTableData(String mainKey, String operationName, String cmdName){
		Object[][] data = null;
		
		LinkedHashMap<CncWebAPIItems,String> cmdInfo = getCmdInfo(mainKey, operationName, cmdName);
		if(null != cmdInfo){
			String[] paras = (null!=cmdInfo.get(CncWebAPIItems.OUTPARAS))?cmdInfo.get(CncWebAPIItems.OUTPARAS).split(PARASEPERATOR):null;
			String[] parasType = (null!=cmdInfo.get(CncWebAPIItems.OUTPARASTYPE))?cmdInfo.get(CncWebAPIItems.OUTPARASTYPE).split(PARASEPERATOR):null;
			String[] parasVal = (null!=cmdInfo.get(CncWebAPIItems.OUTPARASVAL))?cmdInfo.get(CncWebAPIItems.OUTPARASVAL).split(PARASEPERATOR):null;
			
			if(null != paras){
				int rowCnt = paras.length;
				if(rowCnt < MAXOUTPUTPARAS){
					data = new Object[MAXOUTPUTPARAS][4];
				}else{
					data = new Object[rowCnt][4];
				}
				for(int row=0; row<rowCnt; row++){
					data[row][0] = row+1;
					data[row][1] = paras[row];
					data[row][2] = parasType[row];
					data[row][3] = parasVal[row];
				}
				if(rowCnt < MAXOUTPUTPARAS){
					for(int row=rowCnt; row<MAXOUTPUTPARAS; row++){
						data[row][0] = row+1;
						data[row][1] = "";
						data[row][2] = "";
						data[row][3] = "";
					}
				}
			}
		}
		
		if(null == data){
			data = new Object[MAXOUTPUTPARAS][4];
			for(int row=0; row<MAXOUTPUTPARAS; row++){
				data[row][0] = row+1;
				data[row][1] = "";
				data[row][2] = "";
				data[row][3] = "";
			}
		}
		
		return data;
	}
	
	public String[] getAllOperations(String mainKey){
		String[] operations = null;
		String oprs = "";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			for(String key:oprMap.keySet()){
				if(!"Common".equals(key)){
					if("".equals(oprs)){
						oprs = key;
					}else{
						oprs += "," + key;
					}
				}
			}
		}
		
		if("".equals(oprs)){
			operations = new String[]{""};
		}else{
			operations = oprs.split(",");
		}
		
		return operations;
	}
	
	public String[] getAllCommands(String mainKey, String operationName){
		String[] commands = null;
		String cmds = "";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		
		if(null != oprMap){
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operationName);
			if(null != cmdMap){
				for(String cmd:cmdMap.keySet()){
					if(operationName.equals(cmd)) continue;
					if("".equals(cmds)){
						cmds = cmd;
					}else{
						cmds += "," + cmd;
					}
				}
			}
		}
		if(!"".equals(cmds)){
			commands = cmds.split(",");
		}else{
			commands = new String[]{""};
		}
		
		return commands;
	}
	
	@SuppressWarnings("unchecked")
	public String getOperationExecutive(String mainKey, String operationName){
		String executive = "Myself";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operationName);
			if(null != cmdMap){
				if(null!=cmdMap.get(operationName)){
					executive = ((LinkedHashMap<CncWebAPIItems,String>)cmdMap.get(operationName)).get(CncWebAPIItems.OPERATOR);
				}
			}
		}
		
		return executive;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<CncWebAPIItems,String> getCommonCfg(String mainKey){
		LinkedHashMap<CncWebAPIItems,String> commonCfg = null;
		LinkedHashMap<String,Object> oprInfo = getData(mainKey);
		if(null != oprInfo){
			commonCfg = (LinkedHashMap<CncWebAPIItems, String>) oprInfo.get("Common");
		}
		return commonCfg;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<CncWebAPIItems,String> getCmdInfo(String mainKey, String operationName, String cmdName){
		LinkedHashMap<CncWebAPIItems,String> cmdInfo = null;
		LinkedHashMap<String,Object> oprInfo = getData(mainKey);
		if(null != oprInfo){
			LinkedHashMap<String,Object> cmds = (LinkedHashMap<String, Object>) oprInfo.get(operationName);
			if(null != cmds) cmdInfo = (LinkedHashMap<CncWebAPIItems, String>) cmds.get(cmdName);
		}
		return cmdInfo;
	}
	
	@SuppressWarnings("unchecked")
	public String getMainProgramName(String key){
		String programName = "";
		
		LinkedHashMap<CncWebAPIItems,String> commonCfg = null;
		LinkedHashMap<String,Object> oprInfo = getData(key);
		if(null != oprInfo){
			commonCfg = (LinkedHashMap<CncWebAPIItems, String>) oprInfo.get("Common");
			if(null!=commonCfg) programName = commonCfg.get(CncWebAPIItems.NCPROGMAIN);
		}
		
		return programName;
	}
	
	public String getCmdUrl(String mainKey, String cmdName){
		String url = "";
		LinkedHashMap<CncWebAPIItems,String> commonCfg = getCommonCfg(mainKey);
		if(null != commonCfg) url = commonCfg.get(CncWebAPIItems.URL) + (cmdName.indexOf("#")>0?cmdName.split("#")[0]:cmdName);
		return url;
	}
	
	public String getCmdParaVal(String mainKey, String operationName, String cmdName, CncWebAPIItems item){
		String paraVal = "";
		
		LinkedHashMap<CncWebAPIItems,String> cmdInfo = getCmdInfo(mainKey, operationName, cmdName);
		if(null != cmdInfo){
			if(item == CncWebAPIItems.ID) paraVal = cmdInfo.get(CncWebAPIItems.ID);
			if(item == CncWebAPIItems.NAME) paraVal = cmdInfo.get(CncWebAPIItems.NAME);
			if(item == CncWebAPIItems.URL) paraVal = cmdInfo.get(CncWebAPIItems.URL);
			if(item == CncWebAPIItems.INPARAS) paraVal = cmdInfo.get(CncWebAPIItems.INPARAS);
			if(item == CncWebAPIItems.INPARASTYPE) paraVal = cmdInfo.get(CncWebAPIItems.INPARASTYPE);
			if(item == CncWebAPIItems.INPARASVAL) paraVal = cmdInfo.get(CncWebAPIItems.INPARASVAL);
			if(item == CncWebAPIItems.OUTPARAS) paraVal = cmdInfo.get(CncWebAPIItems.OUTPARAS);
			if(item == CncWebAPIItems.OUTPARASTYPE) paraVal = cmdInfo.get(CncWebAPIItems.OUTPARASTYPE);
			if(item == CncWebAPIItems.OUTPARASVAL) paraVal = cmdInfo.get(CncWebAPIItems.OUTPARASVAL);
			if(item == CncWebAPIItems.SENSORCHECK) paraVal = cmdInfo.get(CncWebAPIItems.SENSORCHECK);
		}
		
		return paraVal;
	}
	
	public LinkedHashMap<String,Object> getCmdOutputParas(String mainKey, String operationName, String cmdName){
		LinkedHashMap<String,Object> paras = null;
		
		LinkedHashMap<CncWebAPIItems,String> cmdInfo = getCmdInfo(mainKey, operationName, cmdName);
		if(null != cmdInfo){
			String[] outParas = (null!=cmdInfo.get(CncWebAPIItems.OUTPARAS))?cmdInfo.get(CncWebAPIItems.OUTPARAS).split(PARASEPERATOR):null;
			String[] outParasType = (null!=cmdInfo.get(CncWebAPIItems.OUTPARASTYPE))?cmdInfo.get(CncWebAPIItems.OUTPARASTYPE).split(PARASEPERATOR):null;
			String[] outParasVal = (null!=cmdInfo.get(CncWebAPIItems.OUTPARASVAL))?cmdInfo.get(CncWebAPIItems.OUTPARASVAL).split(PARASEPERATOR):null;
			
			if(null != outParas){
				paras = new LinkedHashMap<String,Object>();
				for(int i=0; i<outParas.length; i++){
					if("Integer".equals(outParasType[i])){
						paras.put(outParas[i], Integer.parseInt(outParasVal[i]));
					}else{
						paras.put(outParas[i], outParasVal[i]);
					}
				}
			}
		}
		
		return paras;
	}
	
	public String getFileContentMD5(String filePath){
		String md5 = "";
		
		File file = new File(filePath);
		if(null != file && file.exists() && file.isFile()){
			try {
				String code = FileUtils.readFileToString(file, "UTF-8");
				code = code.replace("\r\n", "");
				code = code.replace(" ", "");//add by Hui Zhi 2021/12/28
				if(!"".equals(code)) md5 = MathUtils.MD5Encode(code);
			} catch (IOException e) {
				LogUtils.errorLog("CncWebAPI getFileContentMD5("+filePath+") ERR:"+e.getMessage());
			}
		}
		
		return md5;
	}
	
	public String getCmdInputParas(String mainKey, String operationName, String cmdName, String[] reInitParas, String[] reInitVals, boolean rtnJsonString, String reInitJson){
		String rtnStr = "brand=&model=";
		boolean reInitPara = false;
		JSONObject paras = new JSONObject();
		JSONObject program = new JSONObject();
		
		LinkedHashMap<CncWebAPIItems,String> commonCfg = getCommonCfg(mainKey);
		if(null != commonCfg){
			paras.put("brand", commonCfg.get(CncWebAPIItems.BRAND));
			paras.put("model", commonCfg.get(CncWebAPIItems.MODEL));
			rtnStr = "brand="+commonCfg.get(CncWebAPIItems.BRAND)+"&model="+commonCfg.get(CncWebAPIItems.MODEL);
		}
		
		LinkedHashMap<CncWebAPIItems,String> cmdInfo = getCmdInfo(mainKey, operationName, cmdName);
		if(null != cmdInfo){
			String[] inParas = (null!=cmdInfo.get(CncWebAPIItems.INPARAS))?cmdInfo.get(CncWebAPIItems.INPARAS).split(PARASEPERATOR):null;
			String[] inParasType = (null!=cmdInfo.get(CncWebAPIItems.INPARASTYPE))?cmdInfo.get(CncWebAPIItems.INPARASTYPE).split(PARASEPERATOR):null;
			String[] inParasVal = (null!=cmdInfo.get(CncWebAPIItems.INPARASVAL))?cmdInfo.get(CncWebAPIItems.INPARASVAL).split(PARASEPERATOR):null;
			
			if(null != reInitParas && null != reInitVals && reInitParas.length == reInitVals.length) reInitPara = true;
			for(int i=0; null!=inParas && i<inParas.length; i++){
				if(reInitPara){
					for(int j=0; j<reInitParas.length; j++){
						if(inParas[i].equals(reInitParas[j])){
							inParasVal[i] = reInitVals[j];
							break;
						}
					}
				}
				
				if(rtnJsonString){
					if("Integer".equals(inParasType[i])){
						paras.put(inParas[i], Integer.parseInt(inParasVal[i]));
					}else if("Json".equals(inParasType[i])){
						JSONObject jsParas = JSONObject.fromObject(inParasVal[i]);
						@SuppressWarnings("unchecked")
						Iterator<String> iterPara = jsParas.keys();
						
						JSONObject jsonReInitObj = getReInitJSONObject(reInitJson);
						while(iterPara.hasNext()){
							String key = iterPara.next();
							String val = jsParas.getString(key);
							if(null != jsonReInitObj){
								for(Object paraName:jsonReInitObj.keySet()){
									if(key.equals(""+paraName)){
										val = jsonReInitObj.get(paraName)+"";
										break;
									}
								}
							}
							
							if("code".equals(key) || "body".equals(key)){
								if(val.indexOf("#r#n") >= 0){
									program.put(key, val);
								}else{
									File file = new File(val);
									if(null != file && file.exists() && file.isFile()){
										try {
											String code = FileUtils.readFileToString(file, "UTF-8");
											code = code.replace("\r\n", "#r#n");
											
											if(!"".equals(code)){
//												program.put(key, code + "#r#n");
												program.put(key, "#r#n" + code + "#r#n");//2021.11.2 add by Hui Zhi Fang
											}
										} catch (IOException e) {
											LogUtils.errorLog("CncWebAPI getCmdInputParas ERR:"+e.getMessage());
										}
									}
								}
							}else{
								if("NULL".equals(val)) val = "";
								program.put(key, val);
							}
						}
					}else{
						paras.put(inParas[i], inParasVal[i]);
					}
				}else{
					rtnStr += "&" + inParas[i] + "=" + inParasVal[i];
				}
			}
			
			if(rtnJsonString){
				if(!program.isEmpty()){
					String s = StringEscapeUtils.escapeJava(program.toString());
					paras.put("data", s);
				}
				rtnStr = paras.toString().replace("\\\\", "").replace("#r#n", "\\\\r\\\\n");
			}
		}
		
		return rtnStr;
	}
	
	private JSONObject getReInitJSONObject(String reInitJson){
		JSONObject jsonObj = null;
		
		try {
			if(null != reInitJson && !"".equals(reInitJson)){
				jsonObj = JSONObject.fromObject(reInitJson);
			}
		} catch (Exception e) {
		}
		
		return jsonObj;
	}
}
