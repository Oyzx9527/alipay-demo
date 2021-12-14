layui.use(['form','jquery','jquery_cookie','layer'], function () {
    var form = layui.form,
        layer = layui.layer,
        $ = layui.jquery,
        $ = layui.jquery_cookie($);


    //监听提交
    form.on("submit(login)", function(data){
        var fieldData = data.field;

        if (fieldData.username=='undefinded'||fieldData.username==''){
            layer.msg("用户名不能为空");
            return ;
        }

        if (fieldData.password=='undefinded'||fieldData.password=='') {
            layer.msg("密码不能为空");
            return;
        }
        //发送ajax
        $.ajax({
            //发送方式
            type:"post",
            //发送路径  ctx项目名  发送给user下的login
            url:ctx+"/user/login",
            //准备数据
            data:{
                "userName":fieldData.username,
                "userPwd":fieldData.password
            },
            //响应回来数据类型
            dataType:"json",
            success:function (msg){
                //resultInfo
                if (msg.code==200){
                    //成功的提示
                    //layer.msg("登录成功了",{icon:5});
                    //跳转
                    //window.location.href=ctx+"/main";
                    layer.msg("登录成功了",function (){
                        //将用户数据存储到Cookie
                        $.cookie("userIdStr",msg.result.userIdStr);
                        $.cookie("userName",msg.result.userName);
                        $.cookie("trueName",msg.result.trueName);

                        if ($("input[type='checkbox']").is(":checked")){
                            $.cookie("userIdStr",msg.result.userIdStr,{expires:7});
                            $.cookie("userName",msg.result.userName,{expires:7});
                            $.cookie("trueName",msg.result.trueName,{expires:7});
                        }
                        //跳转页面
                        window.location.href=ctx+"/main";
                    });

                }else {
                    //失败的提示
                    layer.msg(msg.msg);
                }
            }
        });


        //取消默认行为
        return false;
    });
});