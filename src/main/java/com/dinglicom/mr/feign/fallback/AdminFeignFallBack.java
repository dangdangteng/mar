package com.dinglicom.mr.feign.fallback;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dingli.cloudunify.pojo.entity.report.ReportTask;
import com.dinglicom.mr.feign.CloudunifyAdminFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@Log
@Component
public class AdminFeignFallBack implements FallbackFactory<CloudunifyAdminFeign> {
    @Override
    public CloudunifyAdminFeign create(Throwable throwable) {
        return new CloudunifyAdminFeign() {
            @Override
            public Response<ReportTask> findById(int id) {
                return ResponseGenerator.genFailResult("服务异常,稍后在试!");
            }

            @Override
            public Response<ReportTask> findByIdAndState(int id, int state) {
                return ResponseGenerator.genFailResult("服务异常,稍后在试!");
            }

            @Override
            public Response updateReportTaskById(ReportTask reportTask) {
                return ResponseGenerator.genFailResult("服务异常,稍后在试!");
            }
        };
    }
}
