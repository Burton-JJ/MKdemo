package org.mkTest.service.impl;

import org.apache.commons.collections.MapUtils;
import org.mkTest.dao.SeckillDao;
import org.mkTest.dao.SuccessKilledDAO;
import org.mkTest.dao.cache.RedisDao;
import org.mkTest.dto.Exposer;
import org.mkTest.dto.SeckillExecution;
import org.mkTest.entity.Seckill;
import org.mkTest.entity.SuccessKilled;
import org.mkTest.enums.SeckillStateEnum;
import org.mkTest.exception.RepeatKillException;
import org.mkTest.exception.SeckillCloseException;
import org.mkTest.exception.SeckillException;
import org.mkTest.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//注解有@Conponent 通用主键当不知道是@Dao还是@Service还是@Controller时用@Conponent
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDAO successKilledDAO;

    @Autowired
    private RedisDao redisDao;

    //MD5盐值字符串 用于混淆MD5
    private final String slat = "sf;ngs;ioge'pger'gpifbo5&&";//乱输入的


    public List<Seckill> getSeckillList() {
        return seckillDao.queryALL(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        //优化点 ：缓存优化
        //1.访问Redis

        Seckill seckill= redisDao.getSeckill(seckillId);
        if(seckill == null){
            //2.如果缓存中没有 访问数据库
            seckill = seckillDao.queryById(seckillId);
            //如果数据库中不存在
            if (seckill == null){
                return new Exposer(false,seckillId);
            }else{
                //数据库中存在 放入缓存
                redisDao.putSeckill(seckill);
            }
        }


        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        if(nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() >endTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
        //转化特定字符串的过程，不可逆 getMD5加密 方法在下面定义
        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }



    @Transactional
    /**
     * 只有运行期异常 事务才会回滚
     * 使用注解控制事务方法的优点：
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作，RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制
     */
    public SeckillExecution excuteSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, SeckillCloseException, RepeatKillException {

        if( md5 == null || !md5.equals(getMD5(seckillId))){

            throw new SeckillException("seckill data rewrite");

        }
        //执行秒杀逻辑
        Date nowTime = new Date();

        try {
            //记录购买行为
            int insertCount = successKilledDAO.insertSuccessKilled(seckillId, userPhone);
            //这里为复合主键(seckillId, userPhone)
            if(insertCount <= 0){
                //重复秒杀
                throw new RepeatKillException("seckill reapted");
            }else {
                //秒杀成功
                //减库存
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0){
                    //没有更新成功 可能是时间未到或者时间超过了 不在开启状态 秒杀结束 rokkback
                    throw new SeckillCloseException("seckill is closed,not opened");
                }else {
                    //秒杀成功 commit
                    SuccessKilled successKilled = successKilledDAO.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS,successKilled);
                }

            }

        } catch (SeckillCloseException e) {
           throw e;
        } catch (RepeatKillException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            //所有编译器异常转化为运行期异常
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }

    }


    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    public SeckillExecution excuteSeckillProcedure(long seckillId, long userPhone, String md5){

        if( md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStateEnum.DATA_REWRITE);
        }
        Date killTIME = new Date();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTIME);
        map.put("result", null);
        //执行存储过程 result被赋值

        try {
            seckillDao.killByProcedure(map);
            // 获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled sk = successKilledDAO.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, sk);
            } else {
                return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnum.iNNER_ERROR);
        }

    }
}
