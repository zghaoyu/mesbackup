package com.cncmes.test;

import com.cncmes.drv.CncHartFordDrv;
import org.junit.Test;

import java.util.LinkedHashMap;

/**
 * *Zhong
 * *
 */
public class CncTest {
    @Test
    public void macroTest(){
        CncHartFordDrv cncHartFordDrv = new CncHartFordDrv();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
//        cncHartFordDrv.sendCommand("10.10.206.72",8193,"status",null,map);
        Double macro = cncHartFordDrv.getMacro("10.10.206.178",8193,950);
        System.out.println(macro);
    }
}
