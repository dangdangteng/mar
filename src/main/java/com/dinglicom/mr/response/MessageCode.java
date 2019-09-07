package com.dinglicom.mr.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MessageCode<T> {
    private int code;
    private String message;
    private T data;

    public MessageCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
