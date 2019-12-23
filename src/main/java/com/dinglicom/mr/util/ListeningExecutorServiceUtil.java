package com.dinglicom.mr.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ListeningExecutorServiceUtil {
    public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(32, 64, 30L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));

    public static ListeningExecutorService ListenPool() {
        ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(threadPoolExecutor);
        return listeningExecutorService;
    }
}
