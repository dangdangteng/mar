package com.dinglicom.mr.controller.view;

import com.dinglicom.mr.config.IPConfig;
import com.dinglicom.mr.entity.JobMessageListenerEntity;
import com.dinglicom.mr.repository.JobMessageListenerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.Inet4Address;
import java.net.InetAddress;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private IPConfig ipConfig;

    @ResponseBody
    @RequestMapping("/ip")
    public String test() throws Exception {
        int port = ipConfig.getPort();
        InetAddress localHost = Inet4Address.getLocalHost();
        return "ip:" +localHost+ ";" + "port:" + port;
    }
    @Autowired
    private JobMessageListenerRepository jobMessageListenerRepository;

    @RequestMapping("/save")
    public void save() throws Exception{

    }
}
