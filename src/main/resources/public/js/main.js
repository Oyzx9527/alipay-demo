layui.use(['element', 'layer', 'layuimini','jquery','jquery_cookie'], function () {
    var $ = layui.jquery,
        layer = layui.layer,
        $ = layui.jquery_cookie($);

    // 菜单初始化
    $('#layuiminiHomeTabIframe').html('<iframe width="100%" height="100%" frameborder="0"  src="welcome"></iframe>')
    layuimini.initTab();




    //退出操作，选择绑定事件
    $(".login-out").click(function (){
        //清空Cookie
        $.removeCookie("userIdStr",{domain:"localhost",path:"/crm"});
        $.removeCookie("userName",{domain:"localhost",path:"/crm"});
        $.removeCookie("trueName",{domain:"localhost",path:"/crm"});
        //页面跳转

        window.parent.location.href=ctx+"/index";
    })



});