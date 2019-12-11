package com.dinglicom.mr.util;


import com.dingli.cloudunify.core.response.Response;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.java.Log;
import org.checkerframework.checker.nullness.qual.Nullable;

@Log
public class CallBackListenPool implements FutureCallback<Response> {

    @Override
    public void onSuccess(@Nullable Response response) {

        log.info("回调成功!" + response);
    }

    @Override
    public void onFailure(Throwable throwable) {
        log.info("回调失败!" + throwable.toString());
    }
}
