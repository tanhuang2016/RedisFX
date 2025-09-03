package xyz.hashdog.rdm.redis.imp.client;

import redis.clients.jedis.*;
import redis.clients.jedis.json.Path2;
import xyz.hashdog.rdm.redis.client.PipelineAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * redis客户端管道操作实现
 *
 * @author th
 * @version 2.3.2
 * @since 2025/9/3 23:03
 */
public class PipeLineAdapterImpl implements PipelineAdapter {

    private final PipelineBase pipeline;

    private List<Response> responseList;

    public PipeLineAdapterImpl(PipelineBase pipeline) {
        this.pipeline = pipeline;
        this.responseList=new ArrayList<>();
    }

    @Override
    public void memoryUsage(String key, int i) {
        responseList.add(pipeline.memoryUsage(key, i));
    }

    @Override
    public void type(String key) {
        responseList.add(pipeline.type(key));
    }

    @Override
    public void ttl(String key) {
        responseList.add(pipeline.ttl(key));
    }

    @Override
    public void strlen(String key) {
        responseList.add(pipeline.strlen(key));

    }

    @Override
    public void llen(String key) {
        responseList.add(pipeline.llen(key));

    }

    @Override
    public void hlen(String key) {
        responseList.add(pipeline.hlen(key));

    }

    @Override
    public void scard(String key) {
        responseList.add(pipeline.scard(key));

    }

    @Override
    public void zcard(String key) {
        responseList.add(pipeline.zcard(key));

    }

    @Override
    public void xlen(String key) {
        responseList.add(pipeline.xlen(key));

    }

    @Override
    public void jsonObjLen(String key) {
        CommandObjects commandObjects = new CommandObjects();
        responseList.add(pipeline.executeCommand(commandObjects.jsonObjLen(key, Path2.ROOT_PATH)));
    }

    @Override
    public void jsonStrLen(String key) {
        responseList.add(pipeline.jsonStrLen(key));

    }

    @Override
    public void jsonArrLen(String key) {
        responseList.add(pipeline.jsonArrLen(key));
    }

    public List<Object> syncAndReturnAll() {
        pipeline.sync();
        List<Object> result = new ArrayList<>();
        for (Response response : responseList) {
            result.add(response.get());
        }
        return result;
    }
}
