package com.cncmes.test;

import com.sun.jna.Native;

public class JNACallTest {
//	private static Map<String,JNACallTest> jnaMap = new HashMap<String,JNACallTest>();
//	
//	private JNACallTest(String dllName){
//		Native.register(dllName);
//	}
//	
//	/**
//	 * There is one code block only in the memory for the same class
//	 * This way is not working
//	 * @param dllName
//	 * @return
//	 */
//	public static JNACallTest loadLibrary(String dllName){
//		JNACallTest jna = jnaMap.get(dllName);
//		if(null == jna){
//			jna = new JNACallTest(dllName);
//			jnaMap.put(dllName, jna);
//		}
//		
//		return jna;
//	}
	
	public JNACallTest(String dllName){
		Native.register(dllName);
	}
	
	public native int add(int a,int b);
	public native int factorial(int n);
}
