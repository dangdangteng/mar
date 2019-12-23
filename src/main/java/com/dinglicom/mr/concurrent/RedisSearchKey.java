package com.dinglicom.mr.concurrent;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Callable;

public class RedisSearchKey implements Callable<Response<Set<String>>> {

    private Logger logger = LoggerFactory.getLogger(RedisSearchKey.class);

    String key;
    CloudUnifyRedisFeign cloudUnifyRedisFeign;

    public RedisSearchKey(String key, CloudUnifyRedisFeign cloudUnifyRedisFeign) {
        this.key = key;
        this.cloudUnifyRedisFeign = cloudUnifyRedisFeign;
    }


    @Override
    public Response<Set<String>> call() throws Exception {
        Response<Set<String>> setResponse = cloudUnifyRedisFeign.searchKey(key);
        return setResponse;
    }
}