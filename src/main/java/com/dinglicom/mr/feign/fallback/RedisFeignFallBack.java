package com.dinglicom.mr.feign.fallback;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@Log
@Component
@RequestMapping("/fallback/redis/cache")
public class RedisFeignFallBack implements FallbackFactory<CloudUnifyRedisFeign> {

    @Override
    public CloudUnifyRedisFeign create(Throwable throwable) {
        return new CloudUnifyRedisFeign() {
            @Override
            public Response<Set<String>> searchKey(String keyPatten) throws Exception {
                Thread.sleep(5000);
                log.info("retry ----");
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response addString(String objs, String id) {
                try {
                    Thread.sleep(5000);
                    log.info("retry ----");
                } catch (Exception e) {
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response getStringByKey(String key) {
                try {
                    Thread.sleep(5000);
                    log.info("retry ----");
                } catch (Exception e) {
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response<Boolean> remove(String key) {
                try {
                    Thread.sleep(5000);
                    log.info("retry ----");
                } catch (Exception e) {
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response addNum(String objs, String id) {
                try {
                    Thread.sleep(5000);
                    log.info("retry ----");
                } catch (Exception e) {
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response updateDate(String key, String value) {
                try {
                    Thread.sleep(5000);
                    log.info("retry ----");
                } catch (Exception e) {
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }

            @Override
            public Response getNum(String key) {
                try {
                    Thread.sleep(5000);
                    log.info("retry ----");
                } catch (Exception e) {
                    log.info(e.toString());
                }
                return ResponseGenerator.genFailResult("服务器异常");
            }
        };
    }
}
