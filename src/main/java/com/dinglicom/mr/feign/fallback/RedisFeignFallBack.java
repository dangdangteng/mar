package com.dinglicom.mr.feign.fallback;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.Set;

@Log
@Component
public class RedisFeignFallBack implements FallbackFactory<CloudUnifyRedisFeign> {

    @Override
    public CloudUnifyRedisFeign create(Throwable throwable) {
        return new CloudUnifyRedisFeign() {
            @Override
            public Response<Set<String>> searchKey(String keyPatten) throws Exception {
                log.info("retry ---- searchKey");
                log.info("异常信息:" + throwable.toString());
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response addString(String objs, String id) {
                try {
                    log.info("retry ---- addString");
                    log.info("异常信息:" + throwable.toString());
                } catch (Exception e) {
                    log.info("异常信息:" + throwable.toString());
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response getStringByKey(String key) {
                try {
                    log.info("retry ---- getStringByKey");
                    log.info("异常信息:" + throwable.toString());
                } catch (Exception e) {
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response<Long> getLongNum(String key) {
                log.info("获取失败redis 数量失败！");
                return null;
            }

            @Override
            public Response<Boolean> remove(String key) {
                try {
                    log.info("retry ---- remove");
                    log.info("异常信息:" + throwable.toString());
                } catch (Exception e) {
                    log.info("异常信息:" + throwable.toString());
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response addNum(String objs, String id) {
                try {
                    log.info("retry ---- addNum");
                    log.info("异常信息:" + throwable.toString());
                } catch (Exception e) {
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response updateDate(String key, String value) {
                try {
                    log.info("retry ---- updateDate");
                    log.info("异常信息:" + throwable.toString());
                } catch (Exception e) {
                    log.info(e.toString());
                    log.info("异常信息:" + throwable.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response getNum(String key) {
                try {
                    log.info("retry ---- genNum");
                    log.info("异常信息:" + throwable.toString());
                } catch (Exception e) {
                    log.info("异常信息:" + throwable.toString());
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response<Long> incrNum(String key) {
                log.info("自增服务接口访问异常！");
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response<Long> decrNum(String key) {
                log.info("自减服务接口访问异常！");
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response<Long> incrRanNum(String key, long rnum) {
                log.info("随机数自增接口访问异常");
                return ResponseGenerator.genFailResult("服务器异常");
            }
        };
    }
}
