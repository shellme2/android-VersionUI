package com.eebbk.tool.versionupdate;


/**
 * Desc: 版本信息接口
 * Author: llp
 * Create Time: 2016-09-26 16:36
 * Email: jacklulu29@gmail.com
 */

public class SDKVersion {

    /**
     * 获取库名称
     * @return 库名称
     */
    public static String getLibraryName(){
        return Build.LIBRARY_NAME;
    }

    /**
     * 构建时的版本值，如：1, 2, 3, ...
     *
     * @return 版本值
     */
    public static int getSDKInt(){
        return Build.VERSION.SDK_INT;
    }

    /**
     * 版本名称，如：1.0.0, 2.1.2-beta, ...
     *
     * @return 版本名称
     */
    public static String getVersionName(){
        return Build.VERSION.VERSION_NAME;
    }

    /**
     * 构建版本以及时间，主要从git获取,由GIT_TAG + "_" + GIT_SHA + "_" + BUILD_TIME组成
     *
     * @return 构建版本名
     */
    public static String getBuildName(){
        return Build.BUILD_NAME;
    }

    /**
     * 构建时间
     *
     * @return 构建时间
     */
    public static String getBuildTime(){
        return Build.BUILD_TIME;
    }

    /**
     * 构建时的git 标签
     *
     * @return 构建Tag
     */
    public static String getBuildTag(){
        return Build.GIT_TAG;
    }

    /**
     * 构建时的git HEAD值
     *
     * @return 构建Head
     */
    public static String getBuildHead(){
        return Build.GIT_HEAD;
    }

}
