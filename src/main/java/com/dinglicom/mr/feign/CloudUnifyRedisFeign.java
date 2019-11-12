package com.dinglicom.mr.feign;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.feign.fallback.RedisFeignFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Component
@FeignClient(value = "cloudunify-redis-service", fallbackFactory = RedisFeignFallBack.class)
@RequestMapping
public interface CloudUnifyRedisFeign {

    @PostMapping(value = "/redis/cache/getKeys")
    public Response<Set<String>> searchKey(@RequestParam String keyPatten) throws Exception;

    @PostMapping(value = "/redis/cache/addString")
    public Response addString(@RequestParam String objs, @RequestParam String id);

    @PostMapping(value = "/redis/cache/getString")
    public Response getStringByKey(@RequestParam String key);

    @Async
    @PostMapping(value = "/redis/cache/removeKeyAndData")
    public Response<Boolean> remove(@RequestParam String key);

    @PostMapping(value = "/redis/cache/addNum")
    public Response addNum(@RequestParam String objs, @RequestParam String id);

    @PostMapping(value = "/redis/cache/updateDataByKey")
    public Response updateDate(@RequestParam String key,@RequestParam String value);

    @RequestMapping(value = "/redis/cache/getNum")
    public Response getNum(@RequestParam String key);
}
