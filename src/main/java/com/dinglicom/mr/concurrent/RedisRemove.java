package com.dinglicom.mr.concurrent;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class RedisRemove implements Callable<Response> {

    private Logger logger = LoggerFactory.getLogger(RedisRemove.class);

    String key;
    CloudUnifyRedisFeign cloudUnifyRedisFeign;

    public RedisRemove(String key, CloudUnifyRedisFeign cloudUnifyRedisFeign) {
        this.key = key;
        this.cloudUnifyRedisFeign = cloudUnifyRedisFeign;
    }

    @Override
    public Response call() throws Exception {
        logger.info("并发执行redis方法调用");
        Response stringByKey = cloudUnifyRedisFeign.remove(key);
        return stringByKey;
    }
}