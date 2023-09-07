package com.cncmes.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {
	private static ExecutorService es = Executors.newCachedThreadPool();
	private ThreadUtils(){}
	
	public static void Run(Runnable myThread){
		es.execute(myThread);
	}
}
