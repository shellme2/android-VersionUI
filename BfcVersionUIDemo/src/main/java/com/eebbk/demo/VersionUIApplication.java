package com.eebbk.demo;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;


import com.github.moduth.blockcanary.BlockCanary;
import com.squareup.leakcanary.LeakCanary;

/**
 * 作者：haloQ
 * 实现的主要功能：
 * 创建日期：2017/2/9
 * 修改信息：
 */

public class VersionUIApplication extends Application {

    private final boolean DEBUG = true;
    private static Context sContext;

    public static Context getAppContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = this;

        if (DEBUG) {
            //针对线程的监视策略
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            //针对vm
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        BlockCanary.install(this, new AppContext()).start();

        LeakCanary.install(this);
    }
}
