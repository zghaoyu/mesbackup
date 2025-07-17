package com.cncmes.utils;

import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class HttpRequestUtils {
	/**
	 * 
	 * @param url : the url to be accessed
	 * @param parasJsonString : the input parameters in a json string
	 * @param encoding : the encoding method, like utf-8
	 * @param outParas : the return parameters which is normally including the error code and error message
	 * @return The web server response string in json format
	 */
	public static String urlGet(String url, String parasJsonString, String encoding, LinkedHashMap<String,Object> outParas) {
		String result = "";
		BufferedReader in = null;
		try {
			String getParas = "";
			if(null!=parasJsonString && !"".equals(parasJsonString)){
				JSONObject paras = JSONObject.fromObject(parasJsonString);
				@SuppressWarnings("unchecked")
				Iterator<String> key = paras.keys();
				while(key.hasNext()){
					String para = key.next();
					if("".equals(getParas)){
						getParas = para + "=" + paras.getString(para);
					}else{
						getParas += "&" + para + "=" + paras.getString(para);
					}
				}
			}
			if(!"".equals(getParas)) url += "?" + getParas;
			
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			
			// The response content
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			JSONObject rtn = JSONObject.fromObject(parasJsonString);
			if(null != outParas){
				for(String para:outParas.keySet()){
					if(outParas.get(para).getClass().equals(Integer.class)){
						rtn.put(para, 404);
					}else{
						rtn.put(para, e.getMessage());
					}
				}
			}
			result = rtn.toString();
		} finally {
			try {
				if (in != null) in.close();
			} catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param url : the url to be accessed
	 * @param paraJsonString : the input parameters in a json string
	 * @param encoding : the encoding method, like utf-8
	 * @param outParas : the return parameters which is normally including the error code and error message
	 * @return The web server response string in json format
	 */
	public static String urlPost(String url, String paraJsonString, String encoding, LinkedHashMap<String,Object> outParas) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// Post request necessary settings
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			out = new PrintWriter(conn.getOutputStream());
			out.print(paraJsonString);
			out.flush();
			// The response content
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(),encoding));
			
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			JSONObject rtn = JSONObject.fromObject(paraJsonString);
			if(null != outParas){
				for(String para:outParas.keySet()){
					if(outParas.get(para).getClass().equals(Integer.class)){
						rtn.put(para, 404);
					}else{
						rtn.put(para, e.getMessage());
					}
				}
			}
			result = rtn.toString();
		} finally {
			try {
				if (out != null) out.close();
				if (in != null) in.close();
			} catch (IOException e) {
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param url : the url to be accessed
	 * @param parasJsonString : the input parameters in a json string
	 * @param encoding : the encoding method, like utf-8
	 * @param outParas : the return parameters which is normally including the error code and error message
	 * @return The web server response string in json format
	 */
	public static String httpPost(String url, String parasJsonString, String encoding, LinkedHashMap<String,Object> outParas){
		String result = "";
		String errMsg = "";
		boolean errorHappens = false;
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		JSONObject jsonResult = null;
		HttpPost postMethod = new HttpPost(url);
		try {
			if(null != parasJsonString && !"".equals(parasJsonString)){
				StringEntity entity = new StringEntity(parasJsonString, encoding);
				entity.setContentEncoding(encoding);
				entity.setContentType("application/json");
				postMethod.setEntity(entity);
			}
			HttpResponse response = httpClient.execute(postMethod);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				result = EntityUtils.toString(response.getEntity());
			}else{
				errorHappens = true;
				errMsg = response.getStatusLine().getReasonPhrase();
			}
		} catch (Exception e) {
			errorHappens = true;
			errMsg = e.getMessage();
		} finally {
			if(errorHappens){
				jsonResult = JSONObject.fromObject(parasJsonString);
				if(null != outParas){
					for(String para:outParas.keySet()){
						if(outParas.get(para).getClass().equals(Integer.class)){
							jsonResult.put(para, 404);
						}else{
							jsonResult.put(para, errMsg);
						}
					}
				}
				result = jsonResult.toString();
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param url : the url to be accessed
	 * @param parasJsonString : the input parameters in a json string
	 * @param encoding : the encoding method, like utf-8
	 * @param outParas : the return parameters which is normally including the error code and error message
	 * @return The web server response string in json format
	 */
	public static String httpGet(String url, String parasJsonString, String encoding, LinkedHashMap<String,Object> outParas){
		String result = "";
		String errMsg = "";
		boolean errorHappens = false;
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		JSONObject jsonResult = null;
		try {
			String getParas = "";
			if(null!=parasJsonString && !"".equals(parasJsonString)){
				JSONObject paras = JSONObject.fromObject(parasJsonString);
				@SuppressWarnings("unchecked")
				Iterator<String> key = paras.keys();
				while(key.hasNext()){
					String para = key.next();
					if("".equals(getParas)){
						getParas = para + "=" + paras.getString(para);
					}else{
						getParas += "&" + para + "=" + paras.getString(para);
					}
				}
			}
			if(!"".equals(getParas)) url += "?" + getParas;

			HttpGet getMethod = new HttpGet(url);
			HttpResponse response = httpClient.execute(getMethod);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				InputStream inStream = response.getEntity().getContent();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, encoding));
	            StringBuilder strber = new StringBuilder();
	            String line = null;
	            while ((line = reader.readLine()) != null){
	                strber.append(line + "\n");
	            }
	            inStream.close();
	            
				result = strber.toString();
			}else{
				errorHappens = true;
				errMsg = response.getStatusLine().getReasonPhrase();
			}
		} catch (Exception e) {
			errorHappens = true;
			errMsg = e.getMessage();
		} finally {
			if(errorHappens){
				jsonResult = JSONObject.fromObject(parasJsonString);
				if(null != outParas){
					for(String para:outParas.keySet()){
						if(outParas.get(para).getClass().equals(Integer.class)){
							jsonResult.put(para, 404);
						}else{
							jsonResult.put(para, errMsg);
						}
					}
				}
				result = jsonResult.toString();
			}
		}
		
		return result;
	}
}
