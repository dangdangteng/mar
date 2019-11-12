package com.dinglicom.mr.service;

import com.dingli.cloudunify.core.response.Response;
import com.dinglicom.mr.handle.HandleIndexOf;
import org.springframework.stereotype.Service;

@Service("base-report")
public class BaseStationReportService implements HandleIndexOf {
    @Override
    public Response jobDoing(String jsonObj) throws Exception {
        return null;
    }
}
