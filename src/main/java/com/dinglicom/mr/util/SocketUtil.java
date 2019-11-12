package com.dinglicom.mr.util;

import java.io.IOException;
import java.net.Socket;

/**
 * 端口监控,负责判断,gluster是否挂载,socket直连
 */
public class SocketUtil {
    //判断  本地是否挂着文件系统
    public Socket getOpenPort() throws IOException {
        Socket socket = new Socket("localhost",24077);
        return socket;
    }
}
