package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
    /*
    * 登录系统
    *
    * */
    User login(UserLoginDTO userLoginDTO);
}
