package com.dinglicom.mr.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value = "xml.path")
public class Constants {
    public static final int sec= 5;
    public static final int bigSec = 5000;

    @Value(value = "file")
    private String filePath;
}
