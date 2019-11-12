package com.dinglicom.mr.service;

import com.dinglicom.mr.cc.SiteManagerIFace;

public class TestService {

  public static void main(String[] args) {
    //
      try{
          String s = SiteManagerIFace.SITE_MANAGER_I_FACE.CreateSiteManagerHandle("/Users/saber-opensource/Downloads/0a0bb30fba485295e3a507bb695.uk");
          System.out.println(s);
      }catch (Exception e){
          e.printStackTrace();
      }

  }
}
