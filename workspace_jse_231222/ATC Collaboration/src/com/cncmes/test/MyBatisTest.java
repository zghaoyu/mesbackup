package com.cncmes.test;

import com.cncmes.dao.AtcDao;
import com.cncmes.dao.CNCUsersDao;
import com.cncmes.dto.ATC;
import com.cncmes.dto.CNCUsers;
import com.cncmes.utils.MyBatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

/**
 * *Zhong
 * *
 */
public class MyBatisTest {
    //第一步：获得sqlSession对象
    SqlSession sqlSession;
    @Test
    public void test(){
        try {

//            sqlSession = MyBatisUtils.getSqlSession();
//            //方式一：getMapper
//            CNCUsersDao mapper = sqlSession.getMapper(CNCUsersDao.class);
//            CNCUsers userList = mapper.getUserByNameAndPwd("Eddie","0CAAA916CEA00578B08FD9841076F3A6");
//            System.out.println(userList);


            sqlSession = MyBatisUtils.getSqlSession();
            //方式一：getMapper
            AtcDao mapper = sqlSession.getMapper(AtcDao.class);
            List<ATC> atcs = mapper.getAllATC();
            System.out.println(atcs);
        }catch (Exception e){
            e.printStackTrace();
        }finally {

            //关闭sqlSession
            sqlSession.close();
        }

    }

}
