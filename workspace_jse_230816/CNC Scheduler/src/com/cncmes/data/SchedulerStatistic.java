package com.cncmes.data;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cncmes.base.RunningData;
import com.cncmes.base.SchedulerDataItems;

public class SchedulerStatistic extends RunningData<SchedulerDataItems> {
	private static SchedulerStatistic sStatistic = new SchedulerStatistic();
	private SchedulerStatistic(){}
	public static SchedulerStatistic getInstance(){
		return sStatistic;
	}
	
	public String[] getTableTitle(){
		String[] tableTitle = new String[]{"ID","LineName","WorkpieceQty","DrillingQty","MillingQty","TappingQty","DrillingTime","MillingTime","TappingTime"};
		return tableTitle;
	}
	
	public Object[][] getTableData(String line){
		int rowCount = 1;
		int colCount = getTableTitle().length;
		
		Map<String,LinkedHashMap<SchedulerDataItems,Object>> statisticDt = new LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>>();
		Map<String,LinkedHashMap<SchedulerDataItems,Object>> materialData = SchedulerMaterial.getInstance().getDataMap();
		if(materialData.size() > 0){
			String lineName = "";
			String procName = "";
			String simTime = "";
			String[] sTime = null;
			int drillingQty,millingQty,tappingQty,workpieceQty;
			int drillingTime,millingTime,tappingTime;
			
			for(String id:materialData.keySet()){
				LinkedHashMap<SchedulerDataItems,Object> dt = materialData.get(id);
				lineName = (String) dt.get(SchedulerDataItems.LINENAME);
				procName = (String) dt.get(SchedulerDataItems.PROCESSNAME);
				
				drillingQty = 0; millingQty = 0; tappingQty = 0; workpieceQty = 0;
				drillingTime = 0; millingTime = 0; tappingTime = 0;
				LinkedHashMap<SchedulerDataItems,Object> sData = statisticDt.get(lineName);
				if(null != sData){
					drillingQty = (int)sData.get(SchedulerDataItems.QTYDRILLING);
					millingQty = (int)sData.get(SchedulerDataItems.QTYMILLING);
					tappingQty = (int)sData.get(SchedulerDataItems.QTYTAPPING);
					drillingTime = (int)sData.get(SchedulerDataItems.TIMEDRILLING);
					millingTime = (int)sData.get(SchedulerDataItems.TIMEMILLING);
					tappingTime = (int)sData.get(SchedulerDataItems.TIMETAPPING);
					workpieceQty = (int)sData.get(SchedulerDataItems.QTYWORKPIECE);
				}else{
					sData = new LinkedHashMap<SchedulerDataItems,Object>();
				}
				
				simTime = (String)dt.get(SchedulerDataItems.SIMTIME);
				if("DR".equals(procName)){
					drillingQty++;
					if(simTime.indexOf("|") > 0){
						sTime = simTime.split("|");
						drillingTime += Integer.parseInt(sTime[0]);
					}else{
						drillingTime += Integer.parseInt(simTime);
					}
				}
				
				if("ML".equals(procName)){
					millingQty++;
					if(simTime.indexOf("|") > 0){
						sTime = simTime.split("|");
						millingTime += Integer.parseInt(sTime[0]);
					}else{
						millingTime += Integer.parseInt(simTime);
					}
				}
				
				if("TP".equals(procName)){
					tappingQty++;
					if(simTime.indexOf("|") > 0){
						sTime = simTime.split("|");
						tappingTime += Integer.parseInt(sTime[0]);
					}else{
						tappingTime += Integer.parseInt(simTime);
					}
				}
				
				workpieceQty++;
				sData.put(SchedulerDataItems.QTYDRILLING, drillingQty);
				sData.put(SchedulerDataItems.QTYMILLING, millingQty);
				sData.put(SchedulerDataItems.QTYTAPPING, tappingQty);
				sData.put(SchedulerDataItems.TIMEDRILLING, drillingTime);
				sData.put(SchedulerDataItems.TIMEMILLING, millingTime);
				sData.put(SchedulerDataItems.TIMETAPPING, tappingTime);
				sData.put(SchedulerDataItems.QTYWORKPIECE, workpieceQty);
				
				statisticDt.put(lineName, sData);
			}
		}
		
		Object[][] tableData = new Object[rowCount][colCount];
		if(statisticDt.size() > 0){
			if("All".equals(line)){
				rowCount = statisticDt.size();
				tableData = new Object[rowCount][colCount];
				int row = -1;
				for(String key:statisticDt.keySet()){
					row++;
					LinkedHashMap<SchedulerDataItems,Object> sData = statisticDt.get(key);
					tableData[row][0] = row+1;
					tableData[row][1] = key;
					tableData[row][2] = sData.get(SchedulerDataItems.QTYWORKPIECE);
					tableData[row][3] = sData.get(SchedulerDataItems.QTYDRILLING);
					tableData[row][4] = sData.get(SchedulerDataItems.QTYMILLING);
					tableData[row][5] = sData.get(SchedulerDataItems.QTYTAPPING);
					tableData[row][6] = sData.get(SchedulerDataItems.TIMEDRILLING);
					tableData[row][7] = sData.get(SchedulerDataItems.TIMEMILLING);
					tableData[row][8] = sData.get(SchedulerDataItems.TIMETAPPING);
				}
			}else{
				LinkedHashMap<SchedulerDataItems,Object> sData = statisticDt.get(line);
				if(null != sData){
					tableData[0][0] = 1;
					tableData[0][1] = line;
					tableData[0][2] = sData.get(SchedulerDataItems.QTYWORKPIECE);
					tableData[0][3] = sData.get(SchedulerDataItems.QTYDRILLING);
					tableData[0][4] = sData.get(SchedulerDataItems.QTYMILLING);
					tableData[0][5] = sData.get(SchedulerDataItems.QTYTAPPING);
					tableData[0][6] = sData.get(SchedulerDataItems.TIMEDRILLING);
					tableData[0][7] = sData.get(SchedulerDataItems.TIMEMILLING);
					tableData[0][8] = sData.get(SchedulerDataItems.TIMETAPPING);
				}
			}
		}
		
		return tableData;
	}
}
