package com.dinglicom.mr.handle;

import com.dingli.cloudunify.core.response.Response;

/**
 * @author saber-opensource
 */
public interface HandleIndexOf {
    /**
     * 命中
     */
    public Response jobDoing( String  jsonObj ) throws Exception;
}