package com.cncmes.dao;

import com.cncmes.dto.CNCUsers;

public interface CNCUsersDao {
    CNCUsers getUserByNameAndPwd(String user_name,String user_pwd);
}
