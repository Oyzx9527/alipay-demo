package com.yjxxt.crm.mapper;

import com.yjxxt.crm.base.BaseMapper;
import com.yjxxt.crm.bean.User;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<User,Integer> {


    User selectUserByName(String userName);

    //可自己添加其他条件

    //
    @MapKey("")
    List<Map<String,Object>> selectSales();
}