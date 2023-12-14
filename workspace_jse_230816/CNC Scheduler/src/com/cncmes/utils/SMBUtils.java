package com.cncmes.utils;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import java.io.*;

/**
 * *Zhong
 * *connect with server host and  download processed file
 */
public class SMBUtils {
    //download remote server file to local host
    public static void getRemoteNCProgram(String moCode) {
        InputStream in = null;
        try {
            // 创建远程文件对象
            // smb://ip地址/共享的路径/...
            // smb://用户名:密码@ip地址/共享的路径/...
            String remoteUrl = "smb://w000650:123456@10.10.204.97/NC Program/MoCode/"+moCode+"/";
            SmbFile remoteFile = new SmbFile(remoteUrl);
            remoteFile.connect();//尝试连接
            if (remoteFile.exists()) {
                // 获取共享文件夹中文件列表
                SmbFile[] smbFiles = remoteFile.listFiles();
                if(smbFiles.length<=0)
                {
                    throw new Exception("Please upload NC Program");
                }
                for (SmbFile smbFile : smbFiles) {
                    createFile(smbFile,moCode);
                }
            }else throw new Exception("Please upload NC Program");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void createFile(SmbFile remoteFile,String moCode) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File localFile = new File("D:/NC_Programs/remote/"+moCode+"/" + remoteFile.getName());
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
}
