package com.dinglicom.mr.response;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MessageCode<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public MessageCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
