package com.cncmes.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassUtils {
	public static String[] getClassNames(String packageName) throws URISyntaxException, IOException{
		Set<String> classSet = new HashSet<String>();
		String[] classNames = null;
		
		String packagePath = packageName.replace(".", "/");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL url = classLoader.getResource(packagePath);
		
		if(null != url){
			String protocol = url.getProtocol();
			if("file".equals(protocol)){
				String filePath = "";
				try {
					filePath = URLDecoder.decode(url.getPath(),"UTF-8").substring(1);
				} catch (UnsupportedEncodingException e) {
					throw e;
				}
				String[] files = (new File(filePath)).list();
				
				File file = null;
				String fpath = "";
				for(String f : files){
					fpath = filePath + File.separator + f;
					file = new File(fpath);
					
					if(file.isFile()){
						String fileName = file.getName();
						if(fileName.endsWith(".class") && !fileName.contains("$")){
							classSet.add(packageName+"."+fileName.replace(".class", ""));
						}
					}
				}
			}else if("jar".equals(protocol)){
				String jarPath = URLDecoder.decode(url.getPath(),"UTF-8").replace("!/"+packagePath, "").replace("file:/", "");
				ZipFile zip = new ZipFile(jarPath);
				Enumeration<? extends ZipEntry> entries = zip.entries();
				
				while(entries.hasMoreElements()){
					String className = (new StringBuffer(entries.nextElement().getName())).toString();
					if(className.contains(packagePath) && className.contains(".class") && !className.contains("$")){
						classSet.add(className.replace(".class", "").replace("/", "."));
					}
				}
				zip.close();
			}
		}else{
			classSet.add("com.cncmes.dto.CNCLines");
		}
		
		if(classSet.size() > 0){
			classNames = new String[classSet.size()];
			Iterator<String> it = classSet.iterator();
			int i = -1;
			while(it.hasNext()){
				i++;
				classNames[i] = it.next();
			}
		}
		
		return classNames;
	}
}
