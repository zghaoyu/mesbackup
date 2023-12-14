package com.cncmes.utils;

import java.io.*;

/**
 * *Zhong
 * *
 */
public class FileUtils {
    //read from local host
    public static double getNCProgramTime(String moCode)
    {
        File folder = new File("D:/NC_Programs/remote/"+ moCode);
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
                        time += Double.parseDouble(line.substring(12,20));
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
}
