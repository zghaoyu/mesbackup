package com.cncmes.utils;

import com.cncmes.data.SystemConfig;

import java.io.*;
import java.util.LinkedHashMap;

/**
 * *Zhong
 * *
 */
public class FileUtils {
    public static String RemoteFolder = "";
    public static String LocalFolder = "";
    static {
        SystemConfig sysCfg = SystemConfig.getInstance();
        LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
        RemoteFolder = (String) config.get("RemoterFolderURL");
        LocalFolder = (String) config.get("LocalFolderURL");
    }
    //read from local host
    public static double getNCProgramTime(String moCode)
    {
        File folder = new File(LocalFolder+ moCode);
        File[] files = folder.listFiles();
        double time = 0;
        for (File file : files)
        {

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                //Construct BufferedReader from InputStreamReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                while ((line = br.readLine()) != null)
                {
                    if (line.contains("CYCLE TIME:"))
                    {
                        time += Double.parseDouble(line.substring(line.indexOf(':')+2,line.indexOf("MIN")));
                    }
                }

                br.close();
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return time;
    }
    public static Boolean checkDrawingNo(String moCode,String drwNo)
    {
        File file = new File(LocalFolder+ moCode+".nc");
        if(!file.exists()) return false;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                //Construct BufferedReader from InputStreamReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                while ((line = br.readLine()) != null)
                {
                    if (line.contains("PART NAME"))
                    {
                        String fileDwgNo = line.substring(line.indexOf(':')+2,line.lastIndexOf(')'));
                        if(drwNo.equals(fileDwgNo))
                            return true;
                        else return false;
                    }
                }
                br.close();
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return true;
    }
}
