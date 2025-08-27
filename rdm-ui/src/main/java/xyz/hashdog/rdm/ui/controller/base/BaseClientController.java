package xyz.hashdog.rdm.ui.controller.base;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import xyz.hashdog.rdm.redis.RedisContext;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.ui.entity.PassParameter;

import java.util.function.Function;

/**
 * 需要用到redisClient的子窗口用这个基类
 * @author th
 * @version 1.0.0
 * @since 2023/7/23 22:30
 */
public abstract class BaseClientController<T> extends BaseController<T>{
    /**
     * 当前控制层操作的tab所用的redis客户端连接
     * 此客户端可能是单例,也就是共享的
     */
    protected RedisClient redisClient;
    /**
     * redis上下文,由父类传递绑定
     */
    protected RedisContext redisContext;
    /**
     * 当前db
     */
    protected int  currentDb;

    /**
     * 用于父向子传递db和key
     */
    protected ObjectProperty<PassParameter> parameter = new SimpleObjectProperty<>();

    /**
     * 根上有绑定userdata,setParameter进行绑定操作
     */
    @FXML
    public Node root;

    /**
     * 执行方法
     * 目前用于统一处理jedis执行命令之后的close操作
     *
     * @param execCommand 需要执行的具体逻辑
     * @param <R>         执行jedis命令之后的返回值
     * @return 执行结果
     */
    public  <R> R exeRedis( Function<RedisClient, R> execCommand) {
        if(redisClient.getDb()!=this.currentDb){
            redisClient.select(currentDb);
        }
        return execCommand.apply(redisClient);
    }

    public PassParameter getParameter() {
        return parameter.get();
    }



    public void setParameter(PassParameter parameter) {
        this.redisClient=parameter.getRedisClient();
        this.redisContext=parameter.getRedisContext();
        //数据也需要绑定到根布局上
        root.setUserData(this);
        this.currentDb=parameter.getDb();
        this.parameter.set(parameter);
        paramInitEnd();
    }

     protected void paramInitEnd() {

    }



    public RedisClient getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }


    public RedisContext getRedisContext() {
        return redisContext;
    }


}
