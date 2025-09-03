package xyz.hashdog.rdm.redis.imp.client;

import redis.clients.jedis.PipelineBase;
import redis.clients.jedis.Response;
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

    public List<Object> syncAndReturnAll() {
        pipeline.sync();
        List<Object> result = new ArrayList<>();
        for (Response response : responseList) {
            result.add(response.get());
        }
        return result;
    }
}
