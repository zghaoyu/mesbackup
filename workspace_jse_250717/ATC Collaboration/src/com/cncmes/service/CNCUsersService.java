package com.cncmes.service;

import com.cncmes.dao.CNCUsersDao;
import com.cncmes.dto.CNCUsers;
import com.cncmes.utils.MyBatisUtils;
import org.apache.ibatis.session.SqlSession;

/**
 * *Zhong
 * *
 */
public class CNCUsersService {
    SqlSession sqlSession;
    public CNCUsers getCNCUserByNameAndPwd(String user_name,String user_pwd)
    {
        sqlSession = MyBatisUtils.getSqlSession();
        //方式一：getMapper
        CNCUsersDao mapper = sqlSession.getMapper(CNCUsersDao.class);
        CNCUsers user = mapper.getUserByNameAndPwd(user_name,user_pwd);
        return user;
    }
}
