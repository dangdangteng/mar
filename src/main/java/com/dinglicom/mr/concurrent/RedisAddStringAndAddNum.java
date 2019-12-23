package com.dinglicom.mr.concurrent;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class RedisAddStringAndAddNum implements Callable<Response<String>> {

    String objs;
    String key;
    String type;
    CloudUnifyRedisFeign cloudUnifyRedisFeign;
    private Logger logger = LoggerFactory.getLogger(RedisAddStringAndAddNum.class);

    public RedisAddStringAndAddNum(String objs, String key, String type, CloudUnifyRedisFeign cloudUnifyRedisFeign) {
        this.objs = objs;
        this.key = key;
        this.type = type;
        this.cloudUnifyRedisFeign = cloudUnifyRedisFeign;
    }

    @Override
    public Response<String> call() throws Exception {
        if (type.isEmpty()) {
            return ResponseGenerator.genFailResult("type is null!");
        }
        if (type.equals("addNum")) {
            Response addNum = cloudUnifyRedisFeign.addNum(objs, key);
            return addNum;
        }
        if (type.equals("addString")) {
            Response addString = cloudUnifyRedisFeign.addString(objs, key);
            return addString;
        }
        return ResponseGenerator.genFailResult("type isn't exist !");
    }
}