package com.dinglicom.mr.Enum;

import lombok.Getter;

public enum StatusEnum {
    STATUS_ENUM_Fail(0, "任务失败"),
    STATUS_ENUM_WAITING(1, "任务等待"),
    STATUS_ENUM_M_MQ(2, "master: 已经将任务压入队列"),
    STATUS_ENUM_W_CARRY(3, "worker: 已经开始执行..."),
    STATUS_ENUM_SUCCESS(4, "任务执行成功");


    @Getter
    private Integer stateCode;
    @Getter
    private String requestMessage;

    StatusEnum(Integer stateCode, String requestMessage) {
        this.stateCode = stateCode;
        this.requestMessage = requestMessage;
    }
    public static String getMessage(Integer stateCode) {
        if (stateCode == null) {
            return null;
        }
        if (stateCode >= 0 && stateCode < 6) {
            for (StatusEnum statusEnum : StatusEnum.values()) {
                if (statusEnum.getStateCode().equals(stateCode)) {
                    return statusEnum.getRequestMessage();
                }
            }
        }
        return "stateCode 不合法!";
    }
}
