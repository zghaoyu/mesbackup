package com.cncmes.utils;

import com.cncmes.data.SystemConfig;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import java.io.*;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;

/**
 * *Zhong
 * *connect with server host and  download processed file
 */
public class SMBUtils {
    //download remote server file to local host
//    static String RemoteFolder = "smb://w000650:123456@10.10.204.97/NC Program/MoCode/";
    public static String RemoteFolder = "";
    public static String LocalFolder = "";
    static {
        SystemConfig sysCfg = SystemConfig.getInstance();
        LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
        RemoteFolder = (String) config.get("RemoterFolderURL");
        LocalFolder = (String) config.get("LocalFolderURL");
    }

    public static Boolean getRemoteNCProgram(String moCode) {
        InputStream in = null;
        try {
            // 创建远程文件对象
            // smb://ip地址/共享的路径/...
            // smb://用户名:密码@ip地址/共享的路径/...
            String remoteUrl = RemoteFolder+moCode+".nc";
            SmbFile remoteFile = new SmbFile(remoteUrl);
            remoteFile.connect();//尝试连接
            if (remoteFile.exists()) {
                if(remoteFile.isDirectory())
                {
                    // 获取共享文件夹中文件列表
                    SmbFile[] smbFiles = remoteFile.listFiles();
                    if(smbFiles.length<=0)
                    {
                        System.out.println("Please upload NC Program");
                        return false;
                    }
                    else{
                        for (SmbFile smbFile : smbFiles) {
                            createFile(smbFile,LocalFolder + remoteFile.getName());
                        }
                    }
                }else createFile(remoteFile,LocalFolder+remoteFile.getName());
            }else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    public static void createFile(SmbFile remoteFile,String url) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File localFile = new File(url);
            localFile.getParentFile().mkdirs();
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[8192];
            //读取长度
            int len = 0;
            while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static Boolean getAllFilesFromSMBfolder(String remoteUrl)
    {

        SmbFile remoteFile = null;
        try {
            remoteFile = new SmbFile(remoteUrl);
            remoteFile.connect();//尝试连接
            if(remoteFile.exists()){
                SmbFile[] smbFiles = remoteFile.listFiles();
                for(SmbFile smbFile : smbFiles)
                {
                    String url = LocalFolder + smbFile.getPath().substring(RemoteFolder.length());
                    if(smbFile.isDirectory())
                    {
                        File dire = new File(url);
//                        if(!dire.mkdir()) return false;
                        dire.mkdir();
                        getAllFilesFromSMBfolder(smbFile.getPath());
                    }else {
                        createFile(smbFile,url);
                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
