package com.cncmes.utils;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {
	private static ExecutorService es = Executors.newCachedThreadPool();
	private static ArrayList<Runnable> sqThreads = new ArrayList<Runnable>();
	private ThreadUtils(){}
	
	public static void Run(Runnable myThread){
		es.execute(myThread);
	}
	
	public static void sequentialRun(Runnable myThread, boolean theLastThread){
		if(!theLastThread){
			sqThreads.add(myThread);
		}else{
			if(sqThreads.size() > 0){
				Runnable mainThread = new Runnable(){
					@Override
					public void run() {
						Thread curThread = null;
						for(int i=0; i<sqThreads.size(); i++){
							curThread = new Thread(sqThreads.get(i));
							curThread.start();
							try {
								curThread.join();
							} catch (InterruptedException e) {
							}
						}
						sqThreads.clear();
					}
				};
				es.execute(mainThread);
			}
		}
	}
}
