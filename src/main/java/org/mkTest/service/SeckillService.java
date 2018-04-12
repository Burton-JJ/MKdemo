package org.mkTest.service;

import org.mkTest.dto.Exposer;
import org.mkTest.dto.SeckillExecution;
import org.mkTest.entity.Seckill;
import org.mkTest.exception.RepeatKillException;
import org.mkTest.exception.SeckillCloseException;
import org.mkTest.exception.SeckillException;

import java.util.List;

/**
 * 业务接口：站在使用者角度设计接口
 * 三个方面：方法定义粒度 参数 返回类型
 */
public interface SeckillService {

    //查询所有商品记录
    List<Seckill> getSeckillList();

    //查询单个商品
    Seckill getById(long seckillId);

    //秒杀开启时输出秒杀接口地址，否则输出系统时间和秒杀时间
    Exposer exportSeckillUrl(long seckillId);

    //执行秒杀
    SeckillExecution excuteSeckill(long seckillId, long userPhone, String md5) ;

    //执行秒杀
     SeckillExecution excuteSeckillProcedure(long seckillId, long userPhone, String md5) ;

}
