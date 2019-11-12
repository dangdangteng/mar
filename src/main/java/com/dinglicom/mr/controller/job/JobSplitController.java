package com.dinglicom.mr.controller.job;

import com.dinglicom.mr.entity.DecodeFile;
import com.dinglicom.mr.repository.DecodeFileKidJobRepository;
import com.dinglicom.mr.repository.DecodeFileRepository;
import com.dinglicom.mr.repository.SourceFileRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.service.DDiBJobService;
import com.dinglicom.mr.service.RcuJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/job/spilt")
public class JobSplitController {
    @Autowired
    private RcuJobService rcuJobService;

    @Autowired
    private DDiBJobService dDiBJobService;

    @Autowired
    private DecodeFileKidJobRepository decodeFileKidJobRepository;

    @Autowired
    private DecodeFileRepository decodeFileRepository;

    @Autowired
    private SourceFileRepository sourceFileRepository;

    @RequestMapping(value = "/rcu",method = RequestMethod.POST)
    public MessageCode rcuJobIntoMQ(@RequestParam(value = "id") int id,@RequestParam int port, @RequestParam String filePathName, @RequestParam Integer priority) throws Exception {
        if (priority>= 0 && priority <=100) {
            MessageCode rcu = rcuJobService.rcu(id, filePathName, port, priority);
            return rcu;
        }
        return new MessageCode(0,"消息入队列失败: priority 不在0-100内");
    }
    @RequestMapping(value = "/returnRcu", method = RequestMethod.POST)
    public Boolean returnRcuJobMessage(@RequestParam int port, @RequestParam String filePathName){
        return true;
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public MessageCode test(@RequestParam int id) throws Exception {
        DecodeFile byId = decodeFileRepository.findById(id);
        if (byId.getId() == id){
            MessageCode ddib = dDiBJobService.ddib(id, byId.getFileName(), byId.getPort(), 10);
            return ddib;
        }
        return new MessageCode(0,"id : 输入有误");
    }
}
