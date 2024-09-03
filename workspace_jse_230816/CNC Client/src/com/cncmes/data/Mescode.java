package com.cncmes.data;

import com.cncmes.base.MescodeItems;
import com.cncmes.base.RunningData;

/**
 * System MES Codes
 * @author LI ZI LONG
 *
 */
public class Mescode extends RunningData<MescodeItems> {
	private static Mescode mescode = new Mescode();
	private Mescode(){}
	public static Mescode getInstance(){
		return mescode;
	}
}
