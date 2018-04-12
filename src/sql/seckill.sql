-- 秒杀执行存储过程
DELIMITER $$ -- console ；转化为 $$
-- 定义存储过程
-- 参数 in：输入参数  out：输出参数
-- row_count()：返回上一条修改语句影响的行数（select语句除外）
-- row_count：0 未修改数据 >0表示修改的行数 <0 sql错误/未执行sql修改
CREATE PROCEDURE 'seckill'.'execute_seckill'
  (in v_seckill_id BIGINT,in v_phone BIGINT,
  in v_kill_time TIMESTAMP,out r_result INT)
BEGIN
  DECLARE insert_count INT DEFAULT 0;
  START TRANSACTION;
    INSERT IGNORE INTO success_killed(seckill_id, user_phone, state, create_time)
      VALUES (v_seckill_id,v_phone,v_kill_time);
  SELECT row_count() INTO  insert_count;
  IF (insert_count=0) THEN
    ROLLBACK ;
    SET r_result = -1;
  ELSEIF (insert_count<0) THEN
    ROLLBACK ;
    SET r_result = -2;
  ELSE
    UPDATE
      seckill
    SET
      number=number-1
    WHERE seckill_id = v_seckill_id
    AND start_time <= v_kill_time
    AND end_time >= v_kill_time
    AND number > 0;
    SELECT row_count() INTO  insert_count;
    IF (insert_count=0) THEN
      ROLLBACK ;
      SET r_result = -1;
    ELSEIF (insert_count<0) THEN
      ROLLBACK ;
      SET r_result = -2;
    ELSE
      COMMIT ;
      SET r_result = 1;
    END IF ;
  END IF ;
END;
$$
-- 存储过程定义结束
DELIMITER ;
SET @r_result=-3;
-- 执行存储过程
call execute_seckill(1003,13578965234,now(),@r_result);
-- 获取结果
SELECT @r_result

-- 存储过程优点：事务行级锁持有时间减少 一般银行使用
-- 简单的逻辑可以运用存储过程
-- 在mysql本地执行 效率很高
