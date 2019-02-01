package com.eebbk.tool.versionupdate;



/**
 * Desc: 下载库构建信息、版本信息，编译时自动写入
 * Author: llp
 * Create Time: 2016-09-26 15:03
 * Email: jacklulu29@gmail.com
 */

public class Build {

    /**
     * 库名称
     */
    public static final String LIBRARY_NAME = BuildConfig.LIBRARY_NAME;

    /**
     * 构建时的git 标签
     */
    public static final String GIT_TAG = BuildConfig.GIT_TAG;

    /**
     * 构建时的git HEAD值
     */
    public static final String GIT_HEAD = BuildConfig.GIT_HEAD;

    /**
     * 构建时间
     */
    public static final String BUILD_TIME = BuildConfig.BUILD_TIME;

    /**
     * 构建版本以及时间，主要从git获取,由GIT_TAG + "_" + GIT_SHA + "_" + BUILD_TIME组成
     */
    public static final String BUILD_NAME = GIT_TAG + "_" + GIT_HEAD + "_" + BUILD_TIME;

    public static class VERSION {

        /**
         * 构建时的版本值，如：1, 2, 3, ...
         */
        public static final int SDK_INT = BuildConfig.VERSION_CODE;
        /**
         * 版本名称，如：1.0.0, 2.1.2-alpha, ...
         */
        public static final String VERSION_NAME = BuildConfig.VERSION_NAME;

    }

}
