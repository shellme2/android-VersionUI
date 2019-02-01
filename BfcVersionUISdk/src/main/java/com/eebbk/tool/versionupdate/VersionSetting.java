package com.eebbk.tool.versionupdate;

import android.content.Context;
import android.content.SharedPreferences;

import static java.lang.System.currentTimeMillis;

/**
 * 作者：lj
 * 实现的主要功能：
 * 创建日期：16-12-21
 * 修改信息：
 */
class VersionSetting {

    private static final String SP_VERSIONDATA = "versionUpdate";

    private static final String SP_DELAYSYSTIME = "sysTime";
    private static final String SP_IGNORETIMES = "ignoreTimes";
    private static final String SP_CURRENTAPKVERSIONCODE = "apkVersionCode";

    private static final int MAXREMINDONEDAY = 5;
    static final int MAXREMINDFIVEDAY = 8;
    private static final long ONEDAYMSEC = 86400000;
    private static final long FIVEDAYMSEC = 432000000;


    private SharedPreferences mPreferences;

    VersionSetting(Context context) {
        mPreferences = context.getApplicationContext().getSharedPreferences(SP_VERSIONDATA, Context.MODE_PRIVATE);
    }


    void setDelayUpdateVersion() {
        long sysTime = currentTimeMillis();
        SharedPreferences.Editor edit = mPreferences.edit();
        int ignoreTimes = mPreferences.getInt(SP_IGNORETIMES, 0);

        edit.putLong(SP_DELAYSYSTIME, sysTime);
        edit.putInt(SP_IGNORETIMES, ++ignoreTimes);
        edit.apply();
    }

    boolean shouldShowDelayUpdateVersion() {
        long sysTime = mPreferences.getLong(SP_DELAYSYSTIME, 0);
        int ignoreTimes = mPreferences.getInt(SP_IGNORETIMES, 0);
        long currentTimeMillis = System.currentTimeMillis();

        if (ignoreTimes < MAXREMINDONEDAY) {
            if (currentTimeMillis - sysTime < ONEDAYMSEC) {
                return false;
            }
        } else if (ignoreTimes < MAXREMINDFIVEDAY) {
            if (currentTimeMillis - sysTime < FIVEDAYMSEC) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    int getSpIgnoreTimes() {
        return mPreferences.getInt(SP_IGNORETIMES, 0);
    }

    int getVersionCode() {
        return mPreferences.getInt(SP_CURRENTAPKVERSIONCODE, 0);
    }

    void setVersionCode(int code) {
        mPreferences.edit().putInt(SP_CURRENTAPKVERSIONCODE, code).apply();
    }

    void clearPreferences() {
        mPreferences.edit().putLong(SP_DELAYSYSTIME, 0).putInt(SP_IGNORETIMES, 0).apply();
    }
}
