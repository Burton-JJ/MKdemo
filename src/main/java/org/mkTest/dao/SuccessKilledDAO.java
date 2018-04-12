package org.mkTest.dao;
import org.apache.ibatis.annotations.Param;
import org.mkTest.entity.SuccessKilled;

import javax.annotation.Resource;

public interface SuccessKilledDAO {
    //插入购买明细，可过滤重复 双主键
    int insertSuccessKilled(@Param("seckilledId") long seckilledId, @Param("userPhone") long userPhone);

    //根据ID 查询SuccessKilled并携带Seckill秒杀产品对象
    SuccessKilled queryByIdWithSeckill(@Param("seckilledId") long seckilledId,@Param("userPhone") long userPhone);

}

