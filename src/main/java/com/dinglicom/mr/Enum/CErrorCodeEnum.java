package com.dinglicom.mr.Enum;

import lombok.Getter;

public enum CErrorCodeEnum {
    REQUEST_STATE_IDLE(0, "任务等待"),
    REQUEST_STATE_RUNNING(1, "任务正在执行"),
    REQUEST_STATE_FINISHED(2, "任务完成"),
    REQUEST_STATE_CANCEL(3, "任务被取消"),
    REQUEST_STATE_EXCEPTION(4, "任务异常"),
    REQUEST_STATE_WARNING(5, "任务异常");

    @Getter
    private Integer stateCode;
    @Getter
    private String requestMessage;

    CErrorCodeEnum(Integer stateCode, String requestMessage) {
        this.stateCode = stateCode;
        this.requestMessage = requestMessage;
    }

    public static String getMessage(Integer stateCode) {
        if (stateCode == null) {
            return null;
        }
        if (stateCode >= 0 && stateCode < 6) {
            for (CErrorCodeEnum errorCodeEnum : CErrorCodeEnum.values()) {
                if (errorCodeEnum.getStateCode().equals(stateCode)) {
                    return errorCodeEnum.getRequestMessage();
                }
            }
        }
        return "stateCode 不合法!";
    }

    public static void main(String[] args) {
        System.out.println(getMessage(2));
    }
}
