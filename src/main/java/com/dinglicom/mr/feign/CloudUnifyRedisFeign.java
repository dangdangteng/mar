package com.dinglicom.mr.feign;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.feign.fallback.RedisFeignFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Response addString(@RequestBody String objs, @RequestParam String id);

    @PostMapping(value = "/redis/cache/getString")
    public Response<String> getStringByKey(@RequestParam String key);

    @PostMapping(value = "/redis/cache/getLongNum")
    public Response<Long> getLongNum(@RequestParam String key);

    @Async
    @PostMapping(value = "/redis/cache/removeKeyAndData")
    public Response<Boolean> remove(@RequestParam String key);


    @PostMapping(value = "/redis/cache/addNum")
    public Response addNum(@RequestParam String objs, @RequestParam String id);

    @PostMapping(value = "/redis/cache/updateDataByKey")
    public Response updateDate(@RequestParam String key, @RequestParam String value);

    @RequestMapping(value = "/redis/cache/getNum")
    public Response getNum(@RequestParam String key);

    @RequestMapping(value = "/redis/cache/incrNum")
    public Response<Long> incrNum(@RequestParam String key);

    @RequestMapping(value = "/redis/cache/decrNum")
    public Response<Long> decrNum(@RequestParam String key);

    @RequestMapping(value = "/redis/cache/incrRanNum")
    public Response<Long> incrRanNum(@RequestParam String key,@RequestParam long rnum);
}
