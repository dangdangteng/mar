package com.dinglicom.mr.util;


import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.java.Log;
import org.checkerframework.checker.nullness.qual.Nullable;

@Log
public class CallBackListenPool<T> implements FutureCallback<T> {

    @Override
    public void onSuccess(@Nullable T t) {
        log.info("回调成功!" + t);
    }

    @Override
    public void onFailure(Throwable throwable) {
        log.info("回调失败!" + throwable.toString());
    }
}
