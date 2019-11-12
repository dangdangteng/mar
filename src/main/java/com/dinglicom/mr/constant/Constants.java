package com.dinglicom.mr.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {
    public static final int sec = 5;
    public static final int bigSec = 5000;

    /**
     * 高级别队列设置队列最高上限
     */
    public final static int QUEUE_HIGH_PROPRITY_MAX = 99999;
    /**
     * 普通级别队列设置上线，给高级别队列预留出20000的空间
     */
    public final static int QUEUE_COMMON_PROPRITY_MAX = 80000;

    public final static String rucConfigPath = "/home/fleet/fleetSwapDatas/config/decode/";
    public final static String rucTempPath = "/home/fleet/fleetSwapDatas/swapTemps/decode/";
    public final static String rucTempLatePath = "/home/fleet/fleetSwapDatas/template/decode/";
    public final static String rucResultPath = "/home/fleet/fleetSwapDatas/ddib/";

    public final static String ddibConfigPath = "/home/fleet/fleetSwapDatas/config/import/";
    public final static String ddibTempPath = "/home/fleet/fleetSwapDatas/swapTemps/import/";
    public final static String ddibTempLatePath = "/home/fleet/fleetSwapDatas/template/import/";
    public final static String ddibResultPath = "/home/fleet/fleetSwapDatas/import/";
}
