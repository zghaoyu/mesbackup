package com.cncmes.test;


import org.junit.Test;

import com.cncmes.thread.RackMaterialMonitor;

public class RackMaterialMonitorTest {

	@Test
	public void test() {
		RackMaterialMonitor.getInstance().run("TS62A");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
	}

}
