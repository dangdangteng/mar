package com.dinglicom.mr.concurrent;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class RedisGetLong implements Callable<Response<Long>> {

    private Logger logger = LoggerFactory.getLogger(RedisGetLong.class);

    String key;
    CloudUnifyRedisFeign cloudUnifyRedisFeign;

    public RedisGetLong(String key, CloudUnifyRedisFeign cloudUnifyRedisFeign) {
        this.key = key;
        this.cloudUnifyRedisFeign = cloudUnifyRedisFeign;
    }


    @Override
    public Response<Long> call() throws Exception {
        logger.info("并发执行redis方法调用");
        Response<Long> longNum = cloudUnifyRedisFeign.getLongNum(key);
        logger.info("这个不是空" + longNum.toString());
        return longNum;
    }
}
