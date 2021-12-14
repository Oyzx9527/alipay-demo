package com.yjxxt.crm.mapper;

import com.yjxxt.crm.base.BaseMapper;
import com.yjxxt.crm.bean.Permission;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission,Integer>{


    int deleteRoleModuleByRoleId(Integer roleId);

    int countRoleMudulesByRoleId(Integer roleId);

    List<Integer> selectModelByRoleId(Integer roleId);

    List<String> selectUserHasRolesHasPermissions(Integer userId);
}
