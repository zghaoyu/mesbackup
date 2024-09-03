package com.cncmes.utils;

import java.sql.SQLException;
import java.util.ArrayList;

import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.dto.CNCLine;
import com.cncmes.dto.CNCMaterial;
import com.cncmes.dto.CNCProcessCard;
import com.cncmes.dto.Fixture;
import com.cncmes.dto.FixtureMaterial;
import com.cncmes.dto.FixtureType;
import com.cncmes.dto.Rack;

/**
 * 
 * @author W000586 Hui Zhi 2022/4/12
 *
 */
public class DataUtils {
	private static String dtoFixture = "com.cncmes.dto.Fixture";
	private static String dtoFixtureMaterial = "com.cncmes.dto.FixtureMaterial";
	private static String dtoFixtureType = "com.cncmes.dto.FixtureType";
	private static String dtoCncMaterial = "com.cncmes.dto.CNCMaterial";
	private static String dtoCncLine = "com.cncmes.dto.CNCLine";
	private static String dtoRack = "com.cncmes.dto.Rack";
	private static String dtoProcessCard = "com.cncmes.dto.CNCProcessCard";

	public static Fixture getFixtureInfo(String fixtureNo) {
		DAO dao = new DAOImpl(dtoFixture);
		ArrayList<Object> vos = null;
		Fixture vo = null;
		try {
			if (null != fixtureNo && !"".equals(fixtureNo)) {
				vos = dao.findByCnd(new String[] { "fixture_no" }, new String[] { fixtureNo });
				if (null != vos) {
					vo = (Fixture) vos.get(0);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vo;
	}

	public static CNCMaterial getMaterial(String materialNo) {
		DAO dao = new DAOImpl(dtoCncMaterial);
		ArrayList<Object> vos = null;
		CNCMaterial vo = null;
		try {
			if (null != materialNo && !"".equals(materialNo)) {
				vos = dao.findByCnd(new String[] { "material_no" }, new String[] { materialNo });
				if (null != vos) {
					vo = (CNCMaterial) vos.get(0);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vo;
	}

	public static CNCMaterial getMaterialById(int materialId) {
		DAO dao = new DAOImpl(dtoCncMaterial);
		CNCMaterial vo = null;
		try {
			if (materialId > 0) {
				vo = (CNCMaterial) dao.findById(materialId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vo;
	}

	public static CNCProcessCard getProcessCard(int id) {
		DAO dao = new DAOImpl(dtoProcessCard);
		CNCProcessCard vo = null;
		try {
			if (id > 0) {
				vo = (CNCProcessCard) dao.findById(id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vo;
	}

	public static boolean saveFixtureMaterial(int fixtureId, int materialId) {
		boolean success = true;
		DAO dao = new DAOImpl(dtoFixtureMaterial);
		FixtureMaterial dto = new FixtureMaterial();
		dto.setFixture_id(fixtureId);
		dto.setMaterial_id(materialId);
		dto.setUser_id(LoginSystem.getUserId());
		dto.setIp_address(NetUtils.getLocalIP());
		dto.setPc_name(NetUtils.getLocalHostName());
		dto.setScan_time(TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		dto.setIs_deleted(0);
		dto.setIs_released(0);
		try {
			dao.add(dto);
		} catch (SQLException e) {
			success = false;
			e.printStackTrace();
		}
		return success;
	}

	public static boolean deleteFixtureMaterialById(int fixtureMaterialId) {
		boolean success = true;
		DAO dao = new DAOImpl(dtoFixtureMaterial);
		FixtureMaterial fixtureMaterial = null;
		try {
			fixtureMaterial = (FixtureMaterial) dao.findById(fixtureMaterialId);
			if (null != fixtureMaterial) {
				fixtureMaterial.setIs_deleted(1);
				fixtureMaterial.setIs_released(1);
				if (1 != dao.update(fixtureMaterial)) {
					success = false;
				}
			}

		} catch (SQLException e) {
			success = false;
			e.printStackTrace();
		}
		return success;
	}


	public static ArrayList<FixtureMaterial> getFixtureMaterial(int fixtureId, int is_released, int is_deleted) {
		ArrayList<Object> vos = null;
		ArrayList<FixtureMaterial> result = new ArrayList<>();
		FixtureMaterial vo;
		DAO dao = new DAOImpl(dtoFixtureMaterial);

		try {
			vos = dao.findByCnd(new String[] { "fixture_id", "is_released", "is_deleted" },
					new String[] { "" + fixtureId, "" + is_released, "" + is_deleted });
			if (null != vos) {
				result = new ArrayList<FixtureMaterial>();
				for (int i = 0; i < vos.size(); i++) {
					vo = (FixtureMaterial) vos.get(i);
					result.add(vo);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;

	}

	public static ArrayList<CNCLine> getAllLine() {
		ArrayList<Object> vos = null;
		ArrayList<CNCLine> result = new ArrayList<>();
		CNCLine vo;
		DAO dao = new DAOImpl(dtoCncLine);
		try {
			vos = dao.findAll();
			if (null != vos) {
				for (int i = 0; i < vos.size(); i++) {
					vo = (CNCLine) vos.get(i);
					result.add(vo);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static ArrayList<Rack> getRackByLineId(int lineId) {
		ArrayList<Object> vos = null;
		ArrayList<Rack> result = new ArrayList<Rack>();
		Rack vo;
		DAO dao = new DAOImpl(dtoRack);
		try {
			vos = dao.findByCnd(new String[] { "line_id" }, new String[] { "" + lineId });
			if (null != vos) {
				
				for (int i = 0; i < vos.size(); i++) {
					vo = (Rack) vos.get(i);
					result.add(vo);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<FixtureType> getFixtureType() {
		ArrayList<Object> vos = null;
		ArrayList<FixtureType> result = new ArrayList<>();
		FixtureType vo;
		DAO dao = new DAOImpl(dtoFixtureType);
		try {
			vos = dao.findAll();
			if (null != vos) {
				for (int i = 0; i < vos.size(); i++) {
					vo = (FixtureType) vos.get(i);
					result.add(vo);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static boolean saveFixture(Fixture fixture){
		boolean success = true;
		DAO dao = new DAOImpl(dtoFixture);
		try {
			dao.add(fixture);
		} catch (SQLException e) {
			success = false;
			e.printStackTrace();
		}
		return success;
	}
}
