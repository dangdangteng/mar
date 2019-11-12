//package com.dinglicom.mr.controller.job;
//
//import com.dingli.cloudunify.core.response.Response;
//import com.dinglicom.mr.handle.HandleContext;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * @author saber-opensource
// * 顶级父接口
// */
//@RestController
//@RequestMapping("/dingli/report")
//public class JobDLiTaskController {
//
//    /**
//     * 顶级接口
//     * @param jsonObject 对象序列化后的json string //id  priority 都要包含
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping("/task")
//    public Response dingLiApi(@RequestBody String jsonObject) throws Exception {
//        Response response = handleContext.doJobAtNow(jsonObject);
//        return response;
//    }
//}
