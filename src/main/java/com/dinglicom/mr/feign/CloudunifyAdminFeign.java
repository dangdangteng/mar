package com.dinglicom.mr.feign;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.pojo.entity.report.ReportTask;
import com.dinglicom.mr.feign.fallback.AdminFeignFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "cloudunify-admin",fallbackFactory = AdminFeignFallBack.class)
@RequestMapping("/admin/in/report")
public interface CloudunifyAdminFeign {
    @GetMapping("/find")
    public Response<ReportTask> findById(@RequestParam int id);

    @GetMapping("/findByIdAndState")
    public Response<ReportTask> findByIdAndState(@RequestParam int id, @RequestParam int state);

    @PostMapping("/updateReportTaskById")
    public Response updateReportTaskById(@RequestBody ReportTask reportTask);
}
