package com.dinglicom.mr.concurrent;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;


public class RedisGet implements Callable<Response<String>> {

    private Logger logger = LoggerFactory.getLogger(RedisGet.class);

    String key;
    CloudUnifyRedisFeign cloudUnifyRedisFeign;

    public RedisGet(String key, CloudUnifyRedisFeign cloudUnifyRedisFeign) {
        this.key = key;
        this.cloudUnifyRedisFeign = cloudUnifyRedisFeign;
    }


    @Override
    public Response<String> call() throws Exception {
        logger.info("并发执行redis方法调用");
        Response<String> stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
        logger.info("这个不是空" + stringByKey.toString());
        return stringByKey;
    }
}
