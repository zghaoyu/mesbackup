package com.cncmes.data;

import com.cncmes.base.RunningData;
import com.cncmes.base.SchedulerItems;

/**
 * Scheduler Configuration
 * @author LI ZI LONG
 *
 */
public class SchedulerCfg extends RunningData<SchedulerItems> {
	private static SchedulerCfg schedulerCfg = new SchedulerCfg();
	private SchedulerCfg(){}
	public static SchedulerCfg getInstance(){
		return schedulerCfg;
	}
}
