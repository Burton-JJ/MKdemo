package org.mkTest.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.mkTest.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    public RedisDao(String ip,int port){

        jedisPool = new JedisPool(ip,port);
    }

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId){
        //Redis 操作逻辑 Jedis相当与数据库中的连接 connecter

        try {
            Jedis jedis = jedisPool.getResource();
            try {

                String key = "seckill:"+seckillId;
                //并没有实现内部序列化
                //get->bytes[]->反序列化->Object(Seckill)
                //采用自定义序列化
                //protostuff:pojo
                byte[] bytes = jedis.get(key.getBytes());
                //缓存中获取到
                if (bytes != null) {

                    //空对象
                    Seckill seckill = schema.newMessage();
                    //seckill 反序列化  protostuff按照schema把bytes数据写入空的seckill对象中
                    ProtobufIOUtil.mergeFrom(bytes, seckill, schema);
                    return seckill;
                }



            } finally {
                //关闭连接
                jedis.close();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        //把对象转化为字节数组写入redis
        //set Object(seckill) ->序列化->byte[]


        try {
            Jedis jedis = jedisPool.getResource();

            try {
                String key = "seckill:"+seckill.getSeckillId();
                byte[] bytes = ProtobufIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存
                int timeout = 60 * 60;
                //result 为缓存返回信息 成功时返回ok 错误时返回错误信息
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            } finally {
                //关闭连接
                jedis.close();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}
