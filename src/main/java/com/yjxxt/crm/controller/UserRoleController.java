package com.yjxxt.crm.controller;

import com.yjxxt.crm.base.BaseController;
import com.yjxxt.crm.bean.UserRole;
import com.yjxxt.crm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("user_role")
public class UserRoleController extends BaseController {

    //引入用户角色的service
    @Autowired
    private UserService userService;


    @RequestMapping("index")
    public String index(UserRole userRole){

        return "role/role";
    }
}
