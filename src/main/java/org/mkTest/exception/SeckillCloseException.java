package org.mkTest.exception;

//秒杀关闭异常 ，秒杀关闭后 不能在进行秒杀 比如库存没了 时间过期了
public class SeckillCloseException extends SeckillException{

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
