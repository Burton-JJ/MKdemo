var seckill = {

    // 封装相关ajax的URL
    URL: {
        now: function () {
            return '/demo/seckill/time/now';
        },
        exposer: function (seckillId) {
            return '/demo/seckill/' + seckillId + '/exposer';
        },
        execution: function (seckillId, md5) {
            return '/demo/seckill/' + seckillId + '/' + md5 + '/execute';
        }
    },

    handleSeckillKill: function (seckillId, node) {
        // 秒杀处理逻辑  获取秒杀地址 控制现实逻辑
        node.hide()
            .html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId), {}, function (result) {
            //在回调函数在中执行交互流程
            console.log('result' + result);
            if (result && result['success']) {
                var exposer = result['data'];
                if (exposer['exposed']) {
                    // 开启秒杀
                    node.show();
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId, md5);
                    console.log('killUrl: ' + killUrl);
                    //执行一次 点击多次无效
                    $('#killBtn').one('click', function () {
                        // 执行秒杀
                        // 1. 禁用按钮
                        $(this).addClass('disabled');
                        // 2. 发送秒杀请求
                        $.post(killUrl, {}, function (result) {
                            if (result && result['success']) {
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                // 显示秒杀结果
                                node.html('<span class="label label-success">' + stateInfo + '</span>');
                            } else {
                                node.html('<span class="label label-danger">秒杀失败</span>');
                            }
                        });
                    });
                   // node.show();
                } else {
                    // 未开启秒杀
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    // 重新进入计时逻辑
                    seckill.countdown(seckillId, now, start, end);
                }
            } else {
                console.log('result: ' + result);
            }
        });
    },

    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },

    countdown: function (seckillId, nowTime, startTime, endTime) {
        var seckillBox = $('#seckill-box');
        if (nowTime > endTime) {
            seckillBox.html('秒杀结束');
        } else if (nowTime < startTime) {
            //秒杀未开始
            var killTime = new Date(startTime + 1000);
            //下面的countdown函数为jquery的函数 调用它
            seckillBox.countdown(killTime, function (event) {
                //设置时间格式
                var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');
                seckillBox.html(format);
                //时间走完 执行下面finish.countdown
            }).on('finish.countdown', function () {
                // 倒计时结束 开始开始秒杀 获取秒杀地址 控制显示逻辑
                // Todo
                seckill.handleSeckillKill(seckillId, seckillBox);
            });
        } else {
            // 执行秒杀
            // Todo
            seckill.handleSeckillKill(seckillId, seckillBox);
        }
    },

    detail: {
        init: function (params) {
            //用户手机验证和登录，计时交互
            // 从cookie当中查找手机号
            var killPhone = $.cookie('killPhone');

            if (!seckill.validatePhone(killPhone)) {
                // 绑定手机号
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    show: true,
                    keyboard: true,
                    backdrop: 'static'
                });

                // 弹出按钮事件绑定
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    //console毫秒一定加//TODO
                    console.log('inputPhone='+inputPhone);//TODO
                    if (seckill.validatePhone(inputPhone)) {
                        // 写入cookie
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/demo/seckill'});
                        window.location.reload();
                    } else {

                        //alert("1111");
                        $('#killMessage').hide().html('<label class="label label-danger">手机号错误</label>').show(300);
                    }
                });
            }


            // 登录成功 计时交互
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(), {}, function (result) {
                if (result && result['success']) {
                    var nowTime = result['data'];
                    //时间 计时交互
                    seckill.countdown(seckillId, nowTime, startTime, endTime);
                } else {
                    console.log('result: ' + result);
                }
            });
        }
    }
}