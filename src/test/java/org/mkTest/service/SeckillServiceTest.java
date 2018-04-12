package org.mkTest.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mkTest.dto.SeckillExecution;
import org.mkTest.entity.Seckill;
import org.mkTest.exception.RepeatKillException;
import org.mkTest.exception.SeckillCloseException;
import org.mkTest.exception.SeckillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mkTest.dto.Exposer;

import java.util.List;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"
})
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {

        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void getById() {

        long id = 1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void testSeckillLogic() {

        long id = 1001;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){
            logger.info("exposer="+exposer);
            long phone = 15896325874L;
            String md5 = exposer.getMd5();
            try {
                SeckillExecution seckillExecution =seckillService.excuteSeckill(id, phone, md5);
                logger.info("seckillExecutionResult={}", seckillExecution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage());
            } catch (SeckillCloseException e) {
                logger.error(e.getMessage());
            }

        }else {//秒杀未开始
            logger.warn("exposer={}"+exposer);

        }


    }

    @Test
    public void excuteSeckillProcedure(){
        long seckillId = 1001;
        long  phone = 13681002564L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.excuteSeckillProcedure(seckillId, phone, md5);
            logger.info(execution.getStateInfo());
        }

    }


}