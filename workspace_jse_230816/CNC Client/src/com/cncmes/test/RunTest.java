package com.cncmes.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import com.cncmes.base.CNC;
import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.ErrorCode;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.ctrl.CncFactory;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.CncData;
import com.cncmes.data.CncDriver;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.MachiningSpec;
import com.cncmes.data.Mescode;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RobotData;
import com.cncmes.data.RobotEthernetCmd;
import com.cncmes.data.WorkpieceData;
import com.cncmes.gui.MyConfirmDialog;
import com.cncmes.utils.AudioUtils;
import com.cncmes.utils.ClassUtils;
import com.cncmes.utils.DTOUtils;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.DesUtils;
import com.cncmes.utils.FTPUtils;
import com.cncmes.utils.HttpRequestUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.LoginSystem;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.MyFileUtils;
import com.cncmes.utils.MySystemUtils;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.TimeUtils;
import com.cncmes.utils.XmlUtils;

import net.sf.json.JSONObject;

public class RunTest {
	@Test
	public void dbTest(){
		String[] cncList = DataUtils.getCNCsByLineName("D2E1D1A");
		Assert.assertNotNull(cncList);
	}
	
	@Test
	public void commonFixtureID(){
		String id = "000000008";
		System.out.println("Common Fixture ID:"+id+MathUtils.calculateCmdConfirmCode(id));
	}
	
	@Test
	public void patternTest(){
		String msg = "";
		msg = "机器人给机床1号位上料失败，请确认重试上料操作是否安全。\r\n\r\n";
		msg += "重试给机床上料操作请点击【是】按钮，\r\n否则请点击【否】终止任务。";
		MyConfirmDialog.showDialog("请确认是否重试【给机床上料】操作", msg);
		if(MyConfirmDialog.OPTION_YES == MyConfirmDialog.getConfirmFlag()){
			System.out.println(NetUtils.pingHost("127.0.0.1"));
		}else{
			String str = "CNC_LOCK;CNC_01;1;1;0";
			System.out.println(str+MathUtils.calculateCmdConfirmCode(str));
		}
	}
	
	@Test
	public void xmlTest(){
		try {
			XmlUtils.parseRobotEthernetCmdXml();
			RobotEthernetCmd robotCmd = RobotEthernetCmd.getInstance();
			String[] cmds = robotCmd.getRetrySequence("R2D2#MORI_DR5A", "CLOSE_B_RETRY_2200");
			if(null!=cmds) System.out.println(cmds.toString());
		} catch (Exception e) {
			System.out.println("RobotR2D2 fails to load system config:"+e.getMessage());
		}
	}
	
	@Test
	public void printChars(){
		int start = 0;
		int stop = 127;
		
		int m = 1;
		for(int i=start; i<=stop; i++){
			m = (i+1) % 16;
			System.out.print((char)i);
			if(0 == m) System.out.println("");
		}
	}
	
	@Test
	public void printStringASCII(){
		String s = "FW,001,", hexStr = "";
		char[] chars = s.toCharArray();
		int ascii = 0;
		int xor = 0;
		
		for(int i=0; i<chars.length; i++){
			ascii = (int)chars[i];
			System.out.print(Integer.toHexString(ascii)+",");
			if(0 == i){
				xor = ascii;
			}else{
				xor = xor ^ ascii;
			}
		}
		
		hexStr = Integer.toHexString(xor);
		System.out.println("cfm="+hexStr+"/"+Integer.parseInt(hexStr, 16));
	}
	
	@Test
	public void queryOnlineIPs(){
		try {
			String[] ips = NetUtils.getOnlineIPs();
			System.out.println(ips[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void arrayCmp(){
		Object[][] arr1 = new Object[3][4];
		Object[][] arr2 = new Object[3][4];
		
		if(arr1[0][0] == arr2[0][0]){
			System.out.println("Array is equal");
		}else{
			System.out.println("Array is not equal");
		}
	}
	
	@Test
	public void md5Test(){
		String str = "FS,1,1,1,0";
		String[] arr = str.split(",");
		System.out.println("arr size="+arr.length+"/"+Integer.parseInt("01"));
		System.out.println("md5="+MathUtils.MD5Encode(str));
	}
	
	@Test
	public void classTest() throws ClassNotFoundException, NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException, URISyntaxException, IOException{
		String[] classNames = ClassUtils.getClassNames("com.cncmes.dto");
		if(null != classNames){
			for(String cls:classNames){
				System.out.println(cls);
			}
		}
	}
	
	@Test
	public void DTOTest() throws ClassNotFoundException, SecurityException, IllegalAccessException, InstantiationException, NoSuchFieldException, URISyntaxException, IOException{
		String[] classNames = ClassUtils.getClassNames("com.cncmes.dto");
		if(null != classNames){
			for(String cls:classNames){
				System.out.println(cls);
			}
			
			Map<String,Object> fieldsMap = DTOUtils.getDTOFields("com.cncmes.dto.CNCMachiningProcess");
			Set<String> set = fieldsMap.keySet();
			Iterator<String> it = set.iterator();
			while(it.hasNext()){
				String key = it.next();
				Object val = fieldsMap.get(key);
				System.out.println(key+"="+val+"/"+val.equals(int.class));
			}
		}
	}
	
	@Test
	public void DAOTest(){
		DAO dao = new DAOImpl("com.cncmes.dto.CNCModels");
		try {
			ArrayList<Object> ms = dao.findByPage(1, 2);
			System.out.println(ms);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void reflectionTest(){
		CNC cnc = CncFactory.getInstance("com.cncmes.drv.CncMORI","BroCncDataHandler","MORI_DR5A");
		Assert.assertNotNull(cnc);
	}
	
	@Test
	public void dataUtilTest(){
		DataUtils.getDeviceDriver();
		
		DataUtils.getDeviceInfo(null);
		String id2 = RackProduct.getInstance().getRackIDsByLineName("D2E1D1A")[0];
		RackProduct.getInstance().updateSlot("D2E1D1A", id2, 1, "M00001");
		RackProduct.getInstance().updateSlot("D2E1D1A", id2, 1, "");
		System.out.println("RackProduct="+RackProduct.getInstance().getEmptySlotsCount("D2E1D1A", id2));
		
		String robotIP = RobotData.getInstance().getFirstKey();
		RobotData.getInstance().updateSlot(robotIP, 1, "M00001");
		RobotData.getInstance().updateSlot(robotIP, 2, "M00001");
		System.out.println("RobotESlots="+RobotData.getInstance().getEmptySlotsCount(robotIP));
		System.out.println("RobotNESlots="+RobotData.getInstance().getNotEmptySlotsCount(robotIP));
		
		String cncIP = CncData.getInstance().getFirstKey();
		System.out.println("WorkZoneQTY="+CncData.getInstance().getWorkzoneQTY(cncIP));
		
		DataUtils.getMachiningSpec();
		System.out.println("SpecCount="+MachiningSpec.getInstance().size());
		
		DataUtils.getMescode();
		System.out.println("MescodeCount="+Mescode.getInstance().size());
		
		WorkpieceData wpData = WorkpieceData.getInstance();
		System.out.println("WorkpieceCount="+wpData.size());
		for(String key:wpData.getDataMap().keySet()){
			String wpID = (String) wpData.getData(key).get(WorkpieceItems.ID);
			wpData.setProcessState(wpID, 1, DeviceState.FINISH);
			System.out.println(wpID+" machining done="+wpData.workpieceIsDone(wpID));
			
			System.out.print(key+":");
			int specid = (int) wpData.getData(key).get(WorkpieceItems.SPECID);
			System.out.print(specid);
			
			int procqty = (int) wpData.getData(key).get(WorkpieceItems.PROCQTY);
			System.out.print("/"+procqty);
			System.out.println("");
			
			SpecItems specItems = SpecItems.PROC1;
			for(int i=1;i<=6;i++){
				if(procqty >= i){
					if(1 == i) specItems = SpecItems.PROC1;
					if(2 == i) specItems = SpecItems.PROC2;
					if(3 == i) specItems = SpecItems.PROC3;
					if(4 == i) specItems = SpecItems.PROC4;
					if(5 == i) specItems = SpecItems.PROC5;
					if(6 == i) specItems = SpecItems.PROC6;
					
					@SuppressWarnings("unchecked")
					LinkedHashMap<SpecItems,Object> spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specid+"").get(specItems);
					System.out.print(specItems+":"+spec.get(SpecItems.PROCESSNAME));
					System.out.print("/"+spec.get(SpecItems.NCMODEL));
					System.out.print("/"+wpData.canMachineByCNC(wpID, "EUMA_EV810", null, (i>1?i:null)));
					System.out.print("/"+wpData.getNextProcSimtime(wpID, "EUMA_EV810", null, (i>1?i:null)));
					System.out.print("/"+spec.get(SpecItems.PROGRAM));
					System.out.print("/"+spec.get(SpecItems.SIMTIME));
					System.out.print("/"+spec.get(SpecItems.SURFACE));
					System.out.println("");
				}
			}
		}
	}
	
	@Test
	public void workpieceDataTest(){
		DataUtils.getMachiningSpec();
		DataUtils.getMescode();
		DataUtils.getDeviceDriver();
		DataUtils.getDeviceInfo(null);
	}
	
	@Test
	public void arrayDimensionTest(){
		String[][] data = new String[2][3];
		String[] temp = "".split(";");
		
		System.out.println(data.length + "/" + data[0].length + " | " + temp.length + "/" + temp[0]);
	}
	
	@Test
	public void rackInfoTest(){
		DataUtils.getRacksInfo(null);
		
		String[] pLineNames = RackProduct.getInstance().getLineNames(true);
		for(String ln:pLineNames){
			System.out.print(ln+",");
		}
		System.out.println("Product Rack");
		
		String[] pRackIDs = RackProduct.getInstance().getRackIDsByLineName("All");
		for(String ln:pRackIDs){
			System.out.print(ln+",");
		}
		System.out.println("Product Rack ID");
	}
	
	@Test
	public void arrayTest(){
		String[] a1 = new String[]{"1","2","3"};
		String[] a2 = new String[]{"1","2","3"};
		
		initArray(a1, a2);
		for(int i=0;i<a1.length;i++){
			System.out.println(a1[i]+"/"+a2[i]);
		}
	}
	
	private void initArray(String[] arr1, String[] arr2){
		arr1[0] = "4";
		arr1[1] = "5";
		arr1[2] = "6";
		arr2 = new String[]{"7","8","9"};
	}
	
	@Test
	public void rackDataTest(){
		DataUtils.getRacksInfo(null);
	}
	
	@Test
	public void httpRequestTest(){
		try {
			XmlUtils.parseCncWebAPIXml();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		CncWebAPI cncWebAPI = CncWebAPI.getInstance();
		String cmdName = cncWebAPI.getCmdParaVal("MORI_DR5A", "getMachineState", "status", CncWebAPIItems.NAME);
		
		String[] reInitParas = new String[]{"ip","port"};
		String[] reInitVals = new String[]{"192.168.0.168","9000"};
		String url = CncWebAPI.getInstance().getCmdUrl("MORI_DR5A", cmdName);
		String paras = CncWebAPI.getInstance().getCmdInputParas("MORI_DR5A", "getMachineState", "status", reInitParas, reInitVals, true, null);
		LinkedHashMap<String,Object> outParas = CncWebAPI.getInstance().getCmdOutputParas("MORI_DR5A", "getMachineState", "status");
		System.out.println(url+"?"+paras);
		
		JSONObject resp = JSONObject.fromObject(HttpRequestUtils.httpPost(url, paras, "utf-8", outParas));
		if(null != outParas){
			for(String para:outParas.keySet()){
				if(outParas.get(para).getClass().equals(Integer.class)){
					System.out.println(para+":"+resp.getInt(para));
				}else{
					System.out.println(para+":"+resp.getString(para));
				}
			}
		}
	}
	
	@Test
	public void fileTest(){
		String path = MySystemUtils.getMemoryDumpPath("10.10.81.38");
		System.out.println("Time passed days = "+MyFileUtils.getFilePassedDays(path));
		
//		String filePath = "\\\\10.10.81.38\\NC_Programs\\BRO_S500Z1\\DR\\S500Z1.nc";
//		filePath = MyFileUtils.chooseFile(null, "Select File", "Select File");
//		System.out.println("filePath:"+filePath);
		
//		File file = new File(filePath);
//		if(null != file && file.exists() && file.isFile()){
//			try {
//				String code = FileUtils.readFileToString(file, "UTF-8");
//				code = code.replace("\r\n", "#r#n");
//				System.out.println(file.lastModified()+"/"+file.length()+"/"+(System.currentTimeMillis()-file.lastModified())/1000+"s: "+code);
//			} catch (IOException e) {
//				System.out.println("ERR:"+e.getMessage());
//			}
//		}
//		
//		System.out.println(System.getProperty("user.dir") + File.separator + "ftpDown");
	}
	
	@Test
	public void ftpTest(){
//		FTPUtils.uploadFile("E:\\STO\\sto00001.bro", "/NCProgram");
//		FTPUtils.downloadFile("/BRO_S500Z1/DR/S500Z1.nc", "C:/NC_Programs/BRO_S500Z1/DR");
		FTPUtils.fileExists("/BRO_S500Z1/DR/S500Z1.nc");
	}
	
	@Test
	public void jsonObjectTest(){
		JSONObject jobj = new JSONObject();
		JSONObject jsTop = new JSONObject();
		
		jsTop.put("brand", "brother");
		jsTop.put("model", "S500Z1");
		
		jobj.put("state", "ON");
		jobj.put("status", "read");
		
		jsTop.put("data", jobj.toString());
		
		for(Object key:jsTop.keySet()){
			System.out.println(key+":"+jsTop.get(key));
		}
	}
	
	@Test
	public void hexToInt(){
		String s = "FW,001,";
		byte[] ascii = s.getBytes();
		int xor = ascii[0];
		
		for(int i=1; i<ascii.length; i++){
			System.out.println(toBeautyFormat(Integer.toBinaryString(xor),8));
			System.out.println(toBeautyFormat(Integer.toBinaryString(ascii[i]),8));
			xor ^= ascii[i];
			System.out.println(toBeautyFormat(Integer.toBinaryString(xor),8));
			System.out.println("------"+toBeautyFormat(Integer.toHexString(xor),2));
		}
		
		String hex = "A";
		System.out.println(""+Integer.parseInt(hex, 16));
	}
	
	private String toBeautyFormat(String binaryString,int totalBits){
		String s = "";
		int zeros = totalBits - binaryString.length();
		
		if(zeros > 0){
			for(int i=0; i<zeros; i++){
				s += "0";
			}
		}
		
		return (s+binaryString);
	}
	
	@Test
	public void robotEthernetCmdTest(){
		XmlUtils.parseRobotEthernetCmdXml();
		
		String s = "G91G28Z0.\r\nG0G91G28X0.Y0.\r\nG0G91X-200.\r\nY-100.\r\nM30";
		System.out.println(MathUtils.MD5Encode(s));
		
		RobotEthernetCmd robotCmd = RobotEthernetCmd.getInstance();
		System.out.println(robotCmd.getCommandStr("R2D2", "pickMaterialFromTray", "PICK_DUT", null, null));
		
		String[] robotCmds = null;
		String[] robotOprs = RobotEthernetCmd.getInstance().getAllOperations("R2D2#MORI_DR5A");
		for(String key:robotOprs){
			String[] cmds = RobotEthernetCmd.getInstance().getAllCommands("R2D2#MORI_DR5A", key);
			if(null == robotCmds){
				robotCmds = cmds;
			}else{
				int len1 = robotCmds.length;
				int len2 = cmds.length;
				robotCmds = Arrays.copyOf(robotCmds, len1+len2);
				System.arraycopy(cmds, 0, robotCmds, len1, len2);
			}
		}
		
		System.out.println(Arrays.toString(robotCmds));
	}
	
	@Test
	public void CncAPIDataHandler(){
		String alarmInfo = "[{\"num\":\"IO0500\",\"message\":\"紧急按钮被激活\"},"
				+ "{\"num\":\"7515\",\"message\":\"切削进给速度超程接通\"},"
				+ "{\"num\":\"7516\",\"message\":\"快速进给超程倍率接通\"}]";
		alarmInfo = alarmInfo.replace("\"", "\\\"");
		
		String[] target = new String[]{"\\\"","},{","[","]"};
		String[] replacement = new String[]{"\"","};{","",""};
		alarmInfo = replaceEx(alarmInfo,target,replacement);
		
		String[] data = alarmInfo.split(";");
		LinkedHashMap<String, String> info = new LinkedHashMap<String, String>();
		if(data.length > 0){
			for(int i=0; i<data.length; i++){
				JSONObject dt = JSONObject.fromObject(data[i]);
				info.put(dt.getString("num"), dt.getString("message"));
			}
		}
		System.out.println(info.toString());
		
		//---------------------------------------------------------------------------
		String machineState = "{\"red\":\"OFF\",\"yello\":\"ON\",\"green\":\"OFF\"}";
		machineState = machineState.replace("\"", "\\\"");
		
		target = new String[]{"\\\""};
		replacement = new String[]{"\""};
		machineState = replaceEx(machineState,target,replacement);
		LinkedHashMap<String, String> state = new LinkedHashMap<String, String>();
		JSONObject jstate = JSONObject.fromObject(machineState);
		@SuppressWarnings("unchecked")
		Iterator<String> iterState = jstate.keys();
		while(iterState.hasNext()){
			String key = iterState.next();
			state.put(key, jstate.getString(key));
		}
		System.out.println(state.toString());
		
		//-----------------------------------------------------------------------------
		String machineParas = "{\"feed_rate\":\"0.00 \","
				+ "\"spindle_speed\":\"0\","
				+ "\"rapid_traverse_override_state\":\"2\","
				+ "\"cutting_feed_override_state\":\" 60\","
				+ "\"spindle_override_state\":\"200\"}";
		machineParas = machineParas.replace("\"", "\\\"");
		
		target = new String[]{"\\\""};
		replacement = new String[]{"\""};
		machineParas = replaceEx(machineParas,target,replacement);
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		JSONObject jparas = JSONObject.fromObject(machineParas);
		@SuppressWarnings("unchecked")
		Iterator<String> iterParas = jparas.keys();
		while(iterParas.hasNext()){
			String key = iterParas.next();
			paras.put(key, jparas.getString(key));
		}
		System.out.println(paras.toString());
		
		//-----------------------------------------------------------------------------
		String machineToolLife = "[{\"num\":\"T01\",\"name\":\"123\",\"length_offset\":\"96.050\",\"length_wear_offset\":\"0.000\",\"cutter_comp\":\"0.000\",\"cutter_wear_offset\":\"0.000\",\"unit\":\"1\",\"initial\":\"0\",\"warning\":\"0\",\"life\":\"999999\"},"
				+ "{\"num\":\"T02\",\"name\":\"12345678901234\",\"length_offset\":\"111.390\",\"length_wear_offset\":\"0.000\",\"cutter_comp\":\"0.000\",\"cutter_wear_offset\":\"0.000\",\"unit\":\"\",\"initial\":\"0\",\"warning\":\"0\",\"life\":\"0\"}]";
		machineToolLife = machineToolLife.replace("\"", "\\\"");
		
		target = new String[]{"\\\"","},{","[","]"};
		replacement = new String[]{"\"","};{","",""};
		machineToolLife = replaceEx(machineToolLife,target,replacement);
		
		String[] tLife = machineToolLife.split(";");
		LinkedHashMap<String, LinkedHashMap<String, String>> toolLife = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		if(tLife.length > 0){
			for(int i=0; i<tLife.length; i++){
				JSONObject dt = JSONObject.fromObject(tLife[i]);
				String mainKey = dt.getString("num");
				@SuppressWarnings("unchecked")
				Iterator<String> iterTool = dt.keys();
				LinkedHashMap<String, String> tool = new LinkedHashMap<String, String>();
				while(iterTool.hasNext()){
					String key = iterTool.next();
					tool.put(key, dt.getString(key));
				}
				toolLife.put(mainKey, tool);
			}
		}
		System.out.println(toolLife.toString());
		
		//-----------------------------------------------------------------------------
		String machineCounter = "[{\"name\":\"1\",\"count\":{\"current\":\"39\",\"end\":\"100\",\"warn\":\"80\"}},"
				+ "{\"name\":\"2\",\"count\":{\"current\":\"20\",\"end\":\"1200\",\"warn\":\"800\"}},"
				+ "{\"name\":\"3\",\"count\":{\"current\":\"333\",\"end\":\"15000\",\"warn\":\"13000\"}},"
				+ "{\"name\":\"4\",\"count\":{\"current\":\"3333\",\"end\":\"60000\",\"warn\":\"4000\"}}]";
		machineCounter = machineCounter.replace("\"", "\\\"");
		
		target = new String[]{"\\\"","},{","[","]"};
		replacement = new String[]{"\"","};{","",""};
		machineCounter = replaceEx(machineCounter,target,replacement);
		
		String[] arrCounter = machineCounter.split(";");
		LinkedHashMap<String, LinkedHashMap<String, String>> counter = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		if(arrCounter.length > 0){
			for(int i=0; i<arrCounter.length; i++){
				JSONObject dt = JSONObject.fromObject(arrCounter[i]);
				String mainKey = dt.getString("name");
				String count = dt.getString("count");
				dt = JSONObject.fromObject(count);
				
				@SuppressWarnings("unchecked")
				Iterator<String> iterCounter = dt.keys();
				LinkedHashMap<String, String> cnter = new LinkedHashMap<String, String>();
				while(iterCounter.hasNext()){
					String key = iterCounter.next();
					cnter.put(key, dt.getString(key));
				}
				counter.put(mainKey, cnter);
			}
		}
		System.out.println(counter.toString());
		
		//-----------------------------------------------------------------------------
		String ncProgramContent = "G91G28Z0.\r\nG0G91G28X0.Y0.\r\nG0G91X-200.\r\nY-100.\r\nM30\r\n";
		String[] content = ncProgramContent.split("\r\n");
		System.out.println(content.length + ":" + ArrayUtils.toString(content));
		
		String saveFileName = "666666.dl";
		if(null != saveFileName && !"".equals(saveFileName)){
			String localPath = System.getProperty("user.dir") + File.separator + "Bro_"+saveFileName;
			try {
				FileOutputStream fos = new FileOutputStream(localPath);
				fos.write(ncProgramContent.getBytes());
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String replaceEx(String oriStr,String[] target,String[] replacement){
		String rslt = oriStr;
		
		if(target.length > 0 && target.length == replacement.length){
			for(int i=0; i<target.length; i++){
				rslt = rslt.replace(target[i], replacement[i]);
			}
		}
		
		return rslt;
	}
	
	@Test
	public void deviceDriverTest(){
		DataUtils.getDeviceDriver();
		CncDriver.getInstance().getCncModelByDriver("");
	}
	
	@Test
	public void logTest(){
		String fileName = "test.log";
		for(int i=0; i<10; i++){
			LogUtils.operationLog(fileName, fileName);
		}
	}
	
	@Test
	public void timeUtilsTest(){
		System.out.println("currentTime: "+TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
	}
	
	@Test
	public void folderTest(){
		LogUtils.clearLog(false);
	}
	
	@Test
	public void audioTest(){
		String audioFilePath = System.getProperty("user.dir") + File.separator + "Need You Now.mp3";
		AudioUtils.playMP3(audioFilePath);
	}
	
	@Test
	public void crlnTest(){
//		String s = "G90G80G17G40G54G01F10000.G49 \n(  PART NAME O 1742992A) \n(  DATE TIME O 02-SEP-17 07O54O18) \n(  OPERATION NAME O ZJ170627B2)\n(  TOOL NAME O EM10C)\n(  TOOLTYPE O MILLING) \n(  CUTTERDIAM O 10.000000) \nG08P0\nM05\nM09\nG08P1\nG91G28Z0 \nG90G54G01F10000. \nS2000M03 \nG01F10000.X-20.Y-50. \nG01Y-70.F1000. \nG01X-50. \nG01Y-90. \nG01X-70. \nG01Y-95. \nG01X-75. \nM05\nM09\nG91G28Z0 \nG91G28X0Y0 \nM30\nG01Y-144.51\nG01X-84.271\nG01Y.85\nG01X-106.61\nG01Y-144.51\nG01X-95.441\nG01Y-154.51\nG01Z-1.12\nG01Y-137.01\nG01X-91.771\nG01Y-6.65\nG01X-99.11 \nG01Y-137.01\nG01X-95.441\nG01Y-144.51\nG01X-84.271\nG01Y.85\nG01X-106.61\nG01Y-144.51\nG01X-95.441\nG01Y-154.51\nG01F10000.Z35. \nM05\nM09\nG91G28Z0 \nM30\nG01F10000.X-55.118 \nG01F10000.Z2.2 \nG01Z-.5F600. \nG01Y-133.26\nG01X-53.23 \nG01Y-10.4\nG01X-57.006\nG01Y-133.26\nG01X-55.118\nG01Y-137.01\nG01X-49.48 \nG01Y-6.65\nG01X-60.756\nG01Y-137.01\nG01X-55.118\nG01Y-144.51\nG01X-41.98 \nG01Y.85\nG01X-68.256\nG01Y-144.51\nG01X-55.118\nG01Y-154.51\nG01Z-1.12\nG01Y-133.26\nG01X-53.23 \nG01Y-10.4\nG01X-57.006\nG01Y-133.26\nG01X-55.118\nG01Y-137.01\nG01X-49.48 \nG01Y-6.65\nG01X-60.756\nG01Y-137.01\nG01X-55.118\nG01Y-144.51\nG01X-41.98 \nG01Y.85\nG01X-68.256\nG01Y-144.51\nG01X-55.118\nG01Y-154.51\nG01F10000.Z35. \nG01F10000.X-76.264 \nG01F10000.Z2.2 \nG01Z-1.03F600. \nG01Y-137.01\nG01X-74.956\nG01Y-87.305\nG01X-77.571";
//		System.out.println(s);
		
		CncWebAPIItems[] webAPIItems = new CncWebAPIItems[]{CncWebAPIItems.BRAND,
				CncWebAPIItems.MODEL,CncWebAPIItems.URL,
				CncWebAPIItems.FTPPORT,CncWebAPIItems.FTPUSER,CncWebAPIItems.FTPPWD};
		for(CncWebAPIItems item:webAPIItems){
			System.out.println(item.name()+":"+item.toString());
		}
	}
	
	@Test
	public void subStringTest(){
		String ncProgramContent = "%ABC#r#nDEF%";
		ncProgramContent = ncProgramContent.replace("#r#n", "\r\n");
		if(ncProgramContent.startsWith("%")) ncProgramContent = ncProgramContent.substring(1);
		if(ncProgramContent.endsWith("%")) ncProgramContent = ncProgramContent.substring(0, ncProgramContent.length()-1);
		
		String s1 = "", s2 = "\r\n", s3="";
		for(int j=0; j<100; j++){
			s1 += "(  CUTTERDIAM O 10.000000)\r\n";
		}
		for(long i=0; i<100; i++){
			s2 += s1;
		}
		for(long i=0; i<18; i++){
			s3 += s2;
		}
//		s3=s3.replace("\r\n", "#r#n");
		LogUtils.debugLog("dummy_", s3);
		
		System.out.println(ncProgramContent);
	}
	
	@Test
	public void arrayTest2(){
		long[] zoneSimulationT = new long[]{3L,4L,5L,6L,7L,8L};
		long[] zoneSimFinishT = new long[6];
		String temp = "";
		for(int i=0; i<zoneSimFinishT.length; i++){
			zoneSimFinishT[i] = 0;
			temp = "";
			for(int j=i; j>=0; j--){
				if("".equals(temp)){
					temp += "" + zoneSimulationT[j];
				}else{
					temp += "+" + zoneSimulationT[j];
				}
				zoneSimFinishT[i] += zoneSimulationT[j];
			}
			System.out.println(temp+"="+zoneSimFinishT[i]);
		}
	}
	
	@Test
	public void errorCodeTest(){
		String myNameSpace = this.getClass().getName();
		System.out.println(myNameSpace + ": " + ErrorCode.MC_NCPROGRAM.getErrDesc());
	}
	
	@Test
	public void desTest(){
		String oriData = "1010101010101010";
		try {
			String encodeDt = DesUtils.encrypt(oriData);
			String decodeDt = DesUtils.decrypt(encodeDt);
			System.out.println(oriData+"/"+encodeDt+"/"+decodeDt);
		} catch (Exception e) {
		}
	}
	
	@Test
	public void generatePwd(){
		String pwd = "www111111";
		System.out.println(MathUtils.MD5Encode(pwd));
		
		for(int i=1; i<=9; i++){
			System.out.println(i+":"+LoginSystem.getPermission(i));
		}
	}
	
	@Test
	public void ipTest(){
		String localIP = NetUtils.getLocalIP();
		String subnet = NetUtils.getSubnetMask();
		System.out.println(localIP.substring(0, localIP.lastIndexOf('.'))+"/"+subnet);
		
		System.out.println(NetUtils.getNetworkStatus("10.10.81.38"));
	}
	
	@Test
	public void mathTest(){
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer>();
		for(int i=0; i<10; i++){
			x.add(i, i);
			if(0==i){
				y.add(0, 15);
			}else{
				y.add(i, i+10);
			}
		}
		MathUtils.lineFitting(x, y);
	}
}
