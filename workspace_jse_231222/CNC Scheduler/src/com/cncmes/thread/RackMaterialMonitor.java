package com.cncmes.thread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.cncmes.base.DeviceState;
import com.cncmes.base.SchedulerDataItems;
import com.cncmes.ctrl.SchedulerServer;
import com.cncmes.data.SchedulerMaterial;
import com.cncmes.dto.CNCRackMaterial;
import com.cncmes.gui.Scheduler;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.ThreadUtils;

/**
 *
 * @author W000586 Hui Zhi Fang 2022/7/7
 *
 */
//TODO TESTING
public class RackMaterialMonitor {
	private static int monitorInterval = 5000; // unit is ms
	private static boolean bTaskMonitorEnabled = true;
	private static RackMaterialMonitor rackMaterialMonitor = new RackMaterialMonitor();

	private RackMaterialMonitor() {
	};

	public static RackMaterialMonitor getInstance() {
		return rackMaterialMonitor;
	}

	public void run(String lineName) {
		MyThread thread = new MyThread();
		thread.setLineName(lineName);
		ThreadUtils.Run(thread);
	}

	public static void setMonitorInterval(int interval) {
		monitorInterval = interval;
	}

	public static void disableMonitor() {
		bTaskMonitorEnabled = false;
	}

	public static void enableMonitor() {
		bTaskMonitorEnabled = true;
	}

	public static boolean monitorIsEnabled() {
		return bTaskMonitorEnabled;
	}

	
	class MyThread implements Runnable {
		private String lineName = "";

		private void setLineName(String lineName) {
			this.lineName = lineName;
		}
		
		private void getMaterialData(){
			ArrayList<CNCRackMaterial> materialList = DataUtils.getCNCRackMaterial();
			Iterator<CNCRackMaterial> it = materialList.iterator();
			while(it.hasNext()){
				CNCRackMaterial material = (CNCRackMaterial) it.next();
				String materialInfo = material.getMaterial_info();
//				System.out.println("at RackMaterialMonitor.mythread.getMaterialData:"+materialInfo);
				setMaterialData(materialInfo.split(","));

			}
		}
		

		
		@Override
		public void run() {
			while (!ThreadController.getSchedulerStopFlag()) {
//				System.out.println("start mythread");
				getMaterialData();
				Scheduler.getInstance().refreshGUI(2);
//				System.out.println("refresh GUI mythread");
				try {
					Thread.sleep(monitorInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
		
		
		
		private void setMaterialData(String[] data){
			SchedulerMaterial sMaterial = SchedulerMaterial.getInstance();
			System.out.println("at RackMaterialMonitor.mythread.setMaterialData:"+ Arrays.deepToString(data));
			String lineName = data[1];
//			SchedulerServer.bMaterialDataSyn.put(lineName, true);
			sMaterial.setData(data[0], SchedulerDataItems.LINENAME, lineName);
			
			Object oriState = sMaterial.getData(data[0]).get(SchedulerDataItems.STATE);
			DeviceState curState = DataUtils.getDevStateByString(data[2]);
			if(null == oriState || null != oriState && (DeviceState)oriState != curState){
				sMaterial.setData(data[0], SchedulerDataItems.STATE, curState);
			}
			
			sMaterial.setData(data[0], SchedulerDataItems.PROCESSQTY, data[3]);
			sMaterial.setData(data[0], SchedulerDataItems.PROCESSNAME, data[4]);
			sMaterial.setData(data[0], SchedulerDataItems.SURFACE, data[5]);
			sMaterial.setData(data[0], SchedulerDataItems.SIMTIME, data[6]);
			sMaterial.setData(data[0], SchedulerDataItems.NCMODEL, data[7]);
			sMaterial.setData(data[0], SchedulerDataItems.PROGRAM, data[8]);
			sMaterial.setData(data[0], SchedulerDataItems.RACKID, data[9]);
			sMaterial.setData(data[0], SchedulerDataItems.SLOTNO, data[10]);
//			if("0".equals(data[11])) SchedulerServer.bMaterialDataSyn.put(lineName, false);
		}

	}

}
