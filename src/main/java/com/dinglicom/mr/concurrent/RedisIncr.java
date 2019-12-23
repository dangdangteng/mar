package com.dinglicom.mr.concurrent;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class RedisIncr implements Callable<Response> {
    private Logger logger = LoggerFactory.getLogger(RedisIncr.class);

    String key;
    CloudUnifyRedisFeign cloudUnifyRedisFeign;

    public RedisIncr(String key, CloudUnifyRedisFeign cloudUnifyRedisFeign) {
        this.key = key;
        this.cloudUnifyRedisFeign = cloudUnifyRedisFeign;
    }

    @Override
    public Response call() throws Exception {
        Response<Long> longResponse = cloudUnifyRedisFeign.incrNum(key);
        return longResponse;
    }
}
