package com.dinglicom.mr.handle;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log
@Component
public class HandleContext {
    private final Map<String, HandleIndexOf> map = new ConcurrentHashMap<>();

    /**
     * 没有继承handleindexof的类 所以暂时会报错
     * @param map
     */
    @Autowired
    public HandleContext(Map<String, HandleIndexOf> map) {
        this.map.clear();
        map.forEach((k, v) -> this.map.put(k, v));
    }

    public Response doJobAtNow(String indexOf, String jsonObj) throws Exception {
        if (!StringUtils.isEmpty(indexOf)) {
            return map.get(indexOf).jobDoing(jsonObj);
        }
        return ResponseGenerator.genFailResult("信息不合法");
    }
}
