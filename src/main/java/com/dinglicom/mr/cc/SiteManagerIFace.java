package com.dinglicom.mr.cc;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface SiteManagerIFace extends Library {
    SiteManagerIFace SITE_MANAGER_I_FACE = (SiteManagerIFace) Native.loadLibrary("/Users/saber-opensource/Downloads/baiyang", SiteManagerIFace.class);

    String CreateSiteManagerHandle(String ukFileName);

    int LoadSiteFile(String handle, String siteFileName, int netType);

    void FreeSiteManagerHandle(String Handle);

    int SetFieldName(String Handle, String StandardName, String OtherName);

}
