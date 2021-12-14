package com.yjxxt.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.bean.UserRole;
import com.yjxxt.crm.mapper.UserMapper;
import com.yjxxt.crm.mapper.UserRoleMapper;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.utils.AssertUtil;
import com.yjxxt.crm.utils.Md5Util;
import com.yjxxt.crm.utils.PhoneUtil;
import com.yjxxt.crm.utils.UserIDBase64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService extends BaseService<User,Integer>{


    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    public UserModel userLogin(String userName,String userPwd){
        //校验
        //调用方法校验
        checkUserLoginParam(userName,userPwd);
        //用户是否存在
        User temp = userMapper.selectUserByName(userName);
        AssertUtil.isTrue(temp==null,"用户不存在");
        //用户的密码是否正确
        checkUserPwd(userPwd,temp.getUserPwd());
        //构建返回对象

        return builderUserInfo(temp);
    }


    //构建返回目标对象
    private UserModel builderUserInfo(User user) {
        //实例化目标对象
        UserModel userModel = new UserModel();
        //加密
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        //返回目标对象
        return userModel;
    }
    //校验用户名密码
    private void checkUserLoginParam(String userName, String userPwd) {
        //用户名不能为空
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空");
        //用户密码不能为空
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"密码不能为空");
    }

    //验证密码
    private void checkUserPwd(String userPwd, String userPwd1) {
        //对输入的密码加密
        userPwd = Md5Util.encode(userPwd);
        //加密后密码与数据库密码对比
        AssertUtil.isTrue(!userPwd.equals(userPwd1),"用户密码不正确");
    }

    public void changUserPwd(Integer userId,String oldPassword,String newPassword,String confirmPwd){
        //前提条件，用户存在，并且登录了   userId
        User user = userMapper.selectByPrimaryKey(userId);
        //密码验证
        checkPasswordParams(user,oldPassword,newPassword,confirmPwd);
        //修改密码
        user.setUserPwd(Md5Util.encode(newPassword));
        //确认修改是否成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user)<1,"修改失败了");
    }

    //修改密码的验证
    private void checkPasswordParams(User user, String oldPassword, String newPassword, String confirmPwd) {
        AssertUtil.isTrue(user==null,"用户未登录或不存在");
        //原始密码不能为空
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"请输入原始密码");
        //原始密码是否正确
        AssertUtil.isTrue(!(user.getUserPwd().equals(Md5Util.encode(oldPassword))),"原始密码不正确");
        //新密码非空
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"新密码不能为空");
        //新密码不能和原始密码一致
        AssertUtil.isTrue(newPassword.equals(oldPassword),"新密码和原始密码不能相同");
        //确认密码非空
        AssertUtil.isTrue(StringUtils.isBlank(confirmPwd),"请输入确认密码");
        //确认密码和新密码一致
        AssertUtil.isTrue(!confirmPwd.equals(newPassword),"确认密码和新密码请保持一致");
    }



    //查询所有的销售人员
    public List<Map<String,Object>> querySales(){
        return userMapper.selectSales();
    }


    //用户模块的列表查询
    public Map<String,Object> findUserByParams(UserQuery userQuery){
        //实例化Map
        Map<String,Object> map = new HashMap<String, Object>();
        //初始化分页单位
        PageHelper.startPage(userQuery.getPage(),userQuery.getLimit());
        //开始分页
        PageInfo<User> plist = new PageInfo<User>(selectByParams(userQuery));
        //准备数据
        map.put("code",0);
        map.put("msg","success");
        map.put("count",plist.getTotal());
        map.put("data",plist.getList());
        //返回目标map
        return map;
    }



    /*
    * 一.验证
    * 1。用户非空
    * 2.邮箱非空
    * 3.手机号非空，格式正确
    *
    * 二.设置默认值
    * is_valid=1
    * createDate  系统时间
    * updateDate  系统时间
    * 密码： 1234---加密
    *
    * 三。添加是否成功的校验
    *
    * */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addUser(User user){

        //验证
        checkUser(user.getUserName(),user.getEmail(),user.getPhone());

        //用户名唯一
        User temp = userMapper.selectUserByName(user.getUserName());
        System.out.println(temp);
        AssertUtil.isTrue(temp!=null,"用户名已经存在");
        //设定默认值
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());

        //密码加密
        user.setUserPwd(Md5Util.encode("123456"));

        //验证是否成功
        AssertUtil.isTrue(insertHasKey(user)<1,"添加失败了");
        //AssertUtil.isTrue(insertSelective(user)<1,"添加失败了");
        System.out.println(user.getId()+"<<<"+user.getRoleIds());
        //得到了角色id就能做中间表操作

        relaionUserRole(user.getId(),user.getRoleIds());
    }

    //关联中间表，对中间表进行操作
    // userId--用户id
    // roleIds---角色id
    //原来的角色数量： 1。没有角色--添加新的角色

    //              2。有角色--在已有的角色上新增  或者减少
    //              。。。。？？？
    //       统计原来是否有角色    有--->删除并重新创建;  没有---->直接添加一个
    private void relaionUserRole(Integer userId, String roleIds) {
        //准备集合存储对象
        List<UserRole> urlist = new ArrayList<UserRole>();
        //userId，roleId
        AssertUtil.isTrue(StringUtils.isBlank(roleIds),"请选择角色信息");
        //统计当前用户有多少个角色
        int count = userRoleMapper.countUserRoleNum(userId);
        //判断是否有角色
        //删除当前用户的角色
        if (count>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色删除失败");
        }
        //删除原来的角色
        String[] RoleStrId = roleIds.split(",");
        //遍历
        for (String rid:RoleStrId){
            //准备对象
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(Integer.parseInt(rid));
            userRole.setCreateDate(new Date());
            userRole.setUpdateDate(new Date());
            //存放到集合
            urlist.add(userRole);
        }

        //批量添加
        AssertUtil.isTrue(userRoleMapper.insertBatch(urlist)!=urlist.size(),"用户角色分配失败");
    }

    private void checkUser(String userName, String email, String phone) {
        //一.验证
        //1。用户非空
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空");
        //2.邮箱非空
        AssertUtil.isTrue(StringUtils.isBlank(email),"邮箱不能为空");
        //3.手机号非空，
        AssertUtil.isTrue(StringUtils.isBlank(phone),"手机号不能为空");
        //手机号格式正确
        AssertUtil.isTrue(!PhoneUtil.isMobile(phone),"请输入合法的手机号");
    }
    //修改
    /*
     * 一.验证
     * 1。当前用户id，有则不能修改
     * ID非空且唯一
     * 2.邮箱非空
     * 3.手机号非空，格式正确
     *
     * 二.设置默认值
     * is_valid=1
     * updateDate  系统时间
     *
     * 三。添加是否成功的校验
     *
     * */
    @Transactional(propagation = Propagation.REQUIRED)
    public void changeUser(User user){
        //验证当前用户id存不存在
        //根据Id获取用户的信息
        User temp = userMapper.selectByPrimaryKey(user.getId());
        //判断
        AssertUtil.isTrue(temp==null,"待修改的记录不存在");

        //验证参数
        checkUser(user.getUserName(),user.getEmail(),user.getPhone());

        User temp2=userMapper.selectUserByName(user.getUserName());
        AssertUtil.isTrue(temp2!=null&& !(temp2.getId().equals(user.getId())),"用户名已存在");

        //设置默认值
        user.setUpdateDate(new Date());
        //判断修改是否成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(user)<1,"修改失败了");

        //
        relaionUserRole(user.getId(),user.getRoleIds());
    }


    //批量删除
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeUserIds(Integer[] ids){
        //验证
        AssertUtil.isTrue(ids==null||ids.length==0,"请选择删除数据");
        //遍历对象
        for(Integer userId:ids){
        //统计当前用户有多少个角色
        int count = userRoleMapper.countUserRoleNum(userId);
        //判断是否有角色
        //删除当前用户的角色
        if (count>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色删除失败");
        }
        }
        //判断删除成功与否
        AssertUtil.isTrue(userMapper.deleteBatch(ids)<1,"删除失败了");
    }
}
