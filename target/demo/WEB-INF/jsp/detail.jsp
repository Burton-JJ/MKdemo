<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%@include file="common/head.jsp" %>
    <title>秒杀详情页</title>
</head>


<!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->
<script src="http://apps.bdimg.com/libs/jquery/2.0.0/jquery.min.js"></script>
<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="http://apps.bdimg.com/libs/bootstrap/3.3.0/js/bootstrap.min.js"></script>
<!-- jQuery cookie操作插件 -->
<script src="http://cdn.bootcss.com/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
<!-- jQery countDonw倒计时插件  -->
<script src="http://cdn.bootcss.com/jquery.countdown/2.1.0/jquery.countdown.min.js"></script>
<!-- 开始编写交互逻辑 -->
<script src="/demo/resources/script/seckill.js" type="text/javascript"></script>
<script type="text/javascript">
    $(function () {
        //使用EL表达式传入参数
        seckill.detail.init({
            seckillId: ${seckill.seckillId},
            startTime: ${seckill.startTime.time},//毫秒
            endTime: ${seckill.endTime.time}
        });
    });
</script>

<body>

<div class="container">

    <div class="panel panel-dafault text-center">

        <div class="panel-heading">

            <h2>${seckill.name}</h2>

        </div>

    </div>

    <div class="panel-body text-center">

        <h2 class="text-danger">

            <span class="glyphicon glyphicon-time"></span>
            <!--倒计时 展示倒计时时间-->
            <span class="glyphicon" id="seckill-box"></span>

        </h2>

    </div>

</div>


<!-- 弹出框 -->

<div id="killPhoneModal" class="modal fade" >

    <div class="modal-dialog" style="z-index: 1041">

        <div class="modal-content">

            <div class="modal-header">

                <h3 class="modal-title text-center">

                    <span class="glyphicon glyphicon-phone"></span>

                </h3>

            </div>

            <div class="modal-body">

                <div class="row">

                    <div class="col-xs-8 col-xs-offset-2">

                        <input type="text" name="killPhone" id="killPhoneKey"

                               placeholder="请输入手机号码" class="form-control">

                    </div>

                </div>

            </div>

            <div class="modal-footer">
                <!--验证是否输入正确号码-->
                <span id="killMessage" class="glyphicon"></span>

                <button type="button" id="killPhoneBtn" class="btn btn-success">

                    <span class="glyphicon glyphicon-phone">Submit</span>

                </button>

            </div>

        </div>

    </div>

</div>


</body>

</html>