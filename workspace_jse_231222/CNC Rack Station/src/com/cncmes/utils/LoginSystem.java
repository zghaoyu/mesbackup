package com.cncmes.utils;

import java.sql.SQLException;
import java.util.ArrayList;

import com.cncmes.base.PermissionItems;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.dto.CNCUsers;
import com.cncmes.gui.panel.InputPanel;

public class LoginSystem {
	private static String userName = "guest";
	private static String userAuthorities = "0";
	private static String userExpiryDate = "19700101";
	private static String userLoginTime = "";
	private static int userId = 0;//add by Hui Zhi 2022/1/10
	
	private static void resetParas(){
		userName = "guest";
		userAuthorities = "0";
		userExpiryDate = "19700101";
		userLoginTime = "";
		userId = 0;//add by Hui Zhi 2022/1/10
	}
	
	public static String userLogin(String name, String pwd){
		String msg = "OK";
		
		pwd = MathUtils.MD5Encode(pwd);
		DAO dao = new DAOImpl("com.cncmes.dto.CNCUsers");
		try {
			ArrayList<Object> vos = dao.findByCnd(new String[]{"user_name","user_pwd"}, new String[]{name,pwd});
			if(null != vos){
				CNCUsers vo = (CNCUsers) vos.get(0);
				userName = vo.getUser_name();
				userAuthorities = vo.getUser_authority();
				userExpiryDate = vo.getUser_expirydate();
				userId = vo.getId();//add by Hui Zhi 2022/1/10
				
				long today = Long.parseLong(TimeUtils.getCurrentDate("yyyyMMdd"));
				long expiry = Long.parseLong(userExpiryDate);
				if(expiry < today) msg = "Login failed:Your account is expired";
				try {
					userAuthorities = DesUtils.decrypt(userAuthorities);
				} catch (Exception e) {
					msg = "Login failed:Authority granted error";
				}
			}else{
				msg = "Login failed:User Name or Password is wrong";
			}
		} catch (SQLException e) {
			msg = "Login failed:"+e.getMessage();
		}
		if(!"OK".equals(msg)){
			resetParas();

		}else{
			userLoginTime = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss");
			InputPanel inputPanel = InputPanel.getInstance();
			inputPanel.refreshButtonsEnabled();
		}
		
		return msg;
	}

	public static void userLogout(){
		resetParas();
	}
	
	public static String getUserName(){
		return userName;
	}
	
	public static String getLoginTime(){
		return userLoginTime;
	}
	
	//add by Hui Zhi 2022/1/10
	public static int getUserId(){
		return userId;
	}
	
	public static boolean userHasLoginned(){
		return ("".equals(userLoginTime)?false:true);
	}
	
	public static boolean accessDenied(PermissionItems permission){
		boolean deny = true;
		int authority = permission.getVal();
		
		if(authority>0 && !"0".equals(userAuthorities) && userAuthorities.length()>=authority){
			String s = userAuthorities.substring(authority-1, authority);
			if("1".equals(s)) deny = false;
		}
		
		return deny;
	}
	
	public static PermissionItems getPermission(int permission){
		PermissionItems p = PermissionItems.DEVMONITORING;
		
		for(PermissionItems pi:PermissionItems.values()){
			if(permission == pi.getVal()){
				p = pi;
				break;
			}
		}
		
		return p;
	}
}
