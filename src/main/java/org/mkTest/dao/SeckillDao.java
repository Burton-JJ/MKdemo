package org.mkTest.dao;

import org.apache.ibatis.annotations.Param;
import org.mkTest.entity.Seckill;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SeckillDao {
    //减库存
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

    //根据id查询秒杀商品
    Seckill queryById(long seckillID);

    //根据偏移量查询秒杀商品列表
    List<Seckill> queryALL(@Param("offet") int offet, @Param("limit") int limit);

    /**
     * 使用存储过程进行秒杀
     * @param ParamMap
     */
    void killByProcedure(Map<String,Object> ParamMap );
}
