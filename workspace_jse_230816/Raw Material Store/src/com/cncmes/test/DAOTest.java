package com.cncmes.test;

import java.sql.SQLException;

import org.junit.Test;

import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.dto.CNCMaterial;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.TimeUtils;

public class DAOTest {

	@Test
	public void testDAO() {
//		DAO dao = new DAOImpl("com.cncmes.dto.CNCProcessCard");
		DAO dao = new DAOImpl("com.cncmes.dto.CNCMaterial");
		String createTime = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss");
		String ip = NetUtils.getLocalIP();
		String pc_name = NetUtils.getLocalHostName();
//		int userId = LoginSystem.getUserId();
//		CNCProcessCard dto = new CNCProcessCard(0, "order1", "drawing1", "part1", userId, ip, pc_name, createTime, 0,
//				"cnc_processcard");

		CNCMaterial dto = new CNCMaterial(0, "11111", 1, 1, ip, pc_name, createTime, 0, "cnc_material");
		
		try {
			dao.add(dto);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
