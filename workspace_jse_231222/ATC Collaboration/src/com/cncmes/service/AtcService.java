package com.cncmes.service;

import com.cncmes.dao.AtcDao;
import com.cncmes.dao.CNCUsersDao;
import com.cncmes.dto.ATC;
import com.cncmes.dto.CNCUsers;
import com.cncmes.utils.MyBatisUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * *Zhong
 * *
 */
public class AtcService {
    SqlSession sqlSession;
    public List<ATC> getAllATC()
    {
        sqlSession = MyBatisUtils.getSqlSession();
        //方式一：getMapper
        AtcDao mapper = sqlSession.getMapper(AtcDao.class);
        List<ATC> atcs = mapper.getAllATC();
        return atcs;
    }
}
