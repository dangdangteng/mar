package com.dinglicom.mr.cc;

import com.dinglicom.mr.constant.Constants;
import com.sun.jna.Library;
import com.sun.jna.Native;

public interface SiteManagerIFace extends Library {
    SiteManagerIFace SITE_MANAGER_I_FACE = (SiteManagerIFace) Native.load(Constants.LibSiteSo, SiteManagerIFace.class);

    /**
     * 创建管理对象句柄
     *
     * @param ukFileName
     * @return
     */
    String CreateSiteManagerHandle(String ukFileName);

    /**
     * 加载基站文件,目前只支持文本文件和uk文件
     *
     * @param handle
     * @param siteFileName
     * @param netType
     * @return
     */
    int LoadSiteFile(String handle, String siteFileName, int netType);

    /**
     * 释放句柄
     *
     * @param Handle
     */
    void FreeSiteManagerHandle(String Handle);

    /**
     * 配置字段名称
     * 注意: 要在调用loadsitefile之前调用该函数完成字段配置
     * @param Handle
     * @param StandardName
     * @param OtherName
     * @return
     */
    int SetFieldName(String Handle, String StandardName, String OtherName);

}
