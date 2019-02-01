package com.eebbk.tool.versionupdate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import com.eebbk.bfc.common.app.ToastUtils;
import com.eebbk.bfc.common.devices.IntentUtils;
import com.eebbk.bfc.common.devices.NetUtils;
import com.eebbk.bfc.common.file.FileUtils;
import com.eebbk.bfc.core.sdk.version.Constants;
import com.eebbk.bfc.core.sdk.version.url.IUrl;
import com.eebbk.bfc.core.sdk.version.url.UrlReleaseImpl;
import com.eebbk.bfc.core.sdk.version.util.log.LogUtils;
import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.download.util.NetworkParseUtil;
import com.eebbk.bfc.sdk.downloadmanager.DownloadController;
import com.eebbk.bfc.sdk.downloadmanager.ITask;
import com.eebbk.bfc.sdk.version.BfcVersion;
import com.eebbk.bfc.sdk.version.VersionConstants;
import com.eebbk.bfc.sdk.version.entity.Version;
import com.eebbk.bfc.sdk.version.entity.VersionInfo;
import com.eebbk.bfc.sdk.version.listener.OnVersionCheckListener;
import com.eebbk.bfc.sdk.version.listener.OnVersionDownloadListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;


public class BfcVersionManager {

    private static final int MSG_SHOW_SILENT_DIALOG = 1;
    private static final int MSG_UPDATE_READY = 2;
    private static final int MSG_PROGRESS_UPDATE = 3;
    private static final int MSG_DOWNLOAD_MOBILE_DATA = 4;
    private static final int MSG_DOWNLOAD_PAUSE = 5;
    private static final int MSG_HAVE_NEW_VERSION = 6;
    private static final int MSG_DOWNLOAD_FAILED = 7;
    private static final int MSG_DOWNLOAD_START = 8;

    private Context mContext = null;
    private BfcVersion mBfcVersion = null;
    private VersionSetting mSettings;
    private NormalUpdateDialogImpl mNormalUpdateDialogImpl;
    private VersionUIListener mVersionUIListener;
    private ITask mDownloadItask;
    private boolean mHasDownload = true;
    private boolean mTag = true;
    private DebugLogListener mDebugLogListener;
    private BfcVersionDialogListener mBfcVersionDialogListener;
    private BfcNotificationCallBack mBfcNotificationCallBack;
    private BfcVersionStateListener mBfcVersionStateListener;

    private boolean mIsAutoUpdate;
    private boolean mIsFailed;
    private boolean mIsDebug;
    private boolean mIsSilentUpdate;
    private IUrl mIUrl;

    private VersionProgressDialogImpl mProgressDialog;
    private NetRequestDialogImpl mRequestDialog;
    private LeakHandler mHandler = new LeakHandler(this);
    private static final String TAG = "BfcVersionManager";

    private BfcVersionManager(Context pContext, boolean isAutoUpdate, boolean isDebug, IUrl url) {
        mContext = pContext;
        mIsAutoUpdate = isAutoUpdate;
        mIsDebug = isDebug;
        mIUrl = url;
        mSettings = new VersionSetting(mContext);

        try {
            DownloadController.getInstance().getGlobalConfig();
        } catch (Exception e) {
            e.printStackTrace();
            DownloadController.init(mContext.getApplicationContext());
        }
        initUpdate();
        LogUtils.i(" " + SDKVersion.getLibraryName() + " init, version: " +
                SDKVersion.getVersionName() + "  code: " + SDKVersion.getSDKInt() +
                " build: " + SDKVersion.getBuildName());
    }

    private void initUpdate() {
        mBfcVersion = new BfcVersion.Builder()
                .setCheckStrategy(VersionConstants.CHECK_STRATEGY_REMOTE_FIRST)
                .setDownloadNetwork(Constants.NETWORK_WIFI)
                .autoDownload(false)
                .setDebugMode(mIsDebug)
                .setUrl(mIUrl)
                .build(mContext);

        mBfcVersion.setOnVersionCheckListener(new OnVersionCheckListener() {
            @Override
            public void onUpdateReady(Context context, final VersionInfo info) {
                LogUtils.i(TAG, "onUpdateReady " + info.toString());
                mIsSilentUpdate = mBfcVersion.isSilentInstall(info);
                if (mVersionUIListener != null) {
                    Message msg = Message.obtain();
                    msg.obj = info.getRemoteVersionName();
                    msg.what = MSG_HAVE_NEW_VERSION;
                    mHandler.sendMessage(msg);
                }
                Message msg = Message.obtain();
                msg.obj = info;
                msg.what = MSG_UPDATE_READY;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onNewVersionChecked(final List<Version> newVersions) {
                debugLog(mContext.getString(R.string.bfc_version_ui_have_new_version));
                Version version = newVersions.get(0);
                if (version == null) {
                    return;
                }
                LogUtils.i(TAG, "onNewVersionChecked " + version.toString());
                mIsSilentUpdate = mBfcVersion.isSilentInstall(version);
                if (mVersionUIListener != null) {
                    Message msg = Message.obtain();
                    msg.obj = version.getVersionName();
                    msg.what = MSG_HAVE_NEW_VERSION;
                    mHandler.sendMessage(msg);
                }
                if (mIsSilentUpdate) {
                    Message msg = Message.obtain();
                    msg.obj = version;
                    msg.what = MSG_SHOW_SILENT_DIALOG;
                    mHandler.sendMessage(msg);
                } else {
                    if (!mIsAutoUpdate) {
                        ToastUtils.getInstance(mContext).s("检测到新版本，后台下载中");
                    }
                    beginDownload(version);
                }
            }

            @Override
            public void onVersionCheckException(String errorCode) {
                if (errorCode.equals(com.eebbk.bfc.sdk.version.error.ErrorCode.SERVICE_CHECK_RESPONSE_NULL) && !mIsAutoUpdate) {
                    ToastUtils.getInstance(mContext).l(R.string.bfc_version_ui_null_version_update_toast);
                    if (mVersionUIListener != null) {
                        Message msg = Message.obtain();
                        msg.obj = "null";
                        msg.what = MSG_HAVE_NEW_VERSION;
                        mHandler.sendMessage(msg);
                    }
                }
            }

            @Override
            public void onCheckOver() {
            }
        });

        mBfcVersion.setOnVersionDownloadListener(new OnVersionDownloadListener() {
            @Override
            public void onDownloadWaiting(ITask task) {
            }

            @Override
            public void onDownloadStarted(ITask task) {
                mHandler.sendEmptyMessage(MSG_DOWNLOAD_START);
            }

            @Override
            public void onDownloadConnected(ITask task, boolean resuming, long finishedSize,
                                            long totalSize) {
            }

            @Override
            public void onDownloading(ITask task, final long finishedSize, final long totalSize) {
                if (mIsSilentUpdate && mProgressDialog != null) {
                    Message msg = Message.obtain();
                    msg.arg1 = (int) (finishedSize * 100 / totalSize);
                    msg.arg2 = (int) Math.floor((double) finishedSize / 1024 / 1024);
                    msg.what = MSG_PROGRESS_UPDATE;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onDownloadPause(final ITask task, String errorCode) {
                if (!mIsSilentUpdate || task == null) {
                    return;
                }
                if (mTag && errorCode.equals(ErrorCode.Values.DOWNLOAD_NETWORK_TYPE_DISALLOWED_BY_REQUESTOR)) {
                    mTag = false;
                    Message msg = Message.obtain();
                    msg.what = MSG_DOWNLOAD_MOBILE_DATA;
                    msg.obj = task;
                    mHandler.sendMessage(msg);
                }
                if (mProgressDialog != null && errorCode.equals(ErrorCode.Values.DOWNLOAD_NETWORK_NO_CONNECTION)) {
                    mHandler.sendEmptyMessage(MSG_DOWNLOAD_PAUSE);
                }
            }

            @Override
            public void onDownloadRetry(ITask task, int retries, String errorCode, Throwable
                    throwable) {
                mHandler.sendEmptyMessage(MSG_DOWNLOAD_START);
            }

            @Override
            public void onDownloadFailure(final ITask task, String errorCode, Throwable
                    throwable) {
                if (mIsSilentUpdate) {
                    mHandler.sendEmptyMessage(MSG_DOWNLOAD_FAILED);
                } else if (!mIsFailed) {
                    DownloadController.getInstance().reloadTask(task);
                    mIsFailed = true;
                }
                //下载失败处理
            }

            @Override
            public void onDownloadSuccess(ITask task) {
                debugLog(mContext.getString(R.string.bfc_version_ui_download_success));
                mHasDownload = false;
                mSettings.clearPreferences();
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    void beginDownload(Version version) {
        if(mContext != null){
            debugLog(mContext.getString(R.string.bfc_version_ui_download_begin));
        }
        if (mDownloadItask == null) {
            ITask.Builder builder = mBfcVersion.getDownloadTaskBuilder(version);
            if (builder == null) {
                return;
            }
            mDownloadItask = builder.setMinProgressTime(500).build();
            DownloadController.getInstance().addTask(mDownloadItask);
        } else {
            DownloadController.getInstance().resumeTask(mDownloadItask);
        }
    }

    void beginProgress(Version version) {
        mProgressDialog = new VersionProgressDialogImpl(mContext, this);
        mProgressDialog.setToTalSizeTV("/" + getKbToMb(version.getFileSize()));
        mProgressDialog.setProgressTV("0MB");
        mProgressDialog.show();
        if (mBfcNotificationCallBack != null) {
            mBfcNotificationCallBack.beginDownload();
        }
    }

    public void onVersionCheck() {
        if (!NetUtils.isConnected(mContext) && !mIsAutoUpdate) {
            ToastUtils.getInstance(mContext).l("网络无连接，请检查网络设置");
            return;
        }
        if (mBfcVersion != null) {
            mBfcVersion.checkVersion();
            mTag = true;
        }
    }

    public void setAutoUpdate(boolean isAutoUpdate) {
        mIsAutoUpdate = isAutoUpdate;
    }

    String getKbToMb(long fileSize) {
        return (int) Math.ceil((double) fileSize / 1024 / 1024) + "MB";
    }

    void useMobile(ITask task) {
        int networkTypes = NetworkParseUtil.addNetworkType(task.getNetworkTypes(), NetworkType.NETWORK_MOBILE);
        DownloadController.getInstance().setNetworkTypes(networkTypes, task);
        DownloadController.getInstance().reloadTask(task);
    }


    /**
     * 自升级普通升级方式对话框
     *
     * @param info 新版本特征
     */
    public void showNormalUpdateDialog(final VersionInfo info) {
        if (mSettings == null) {
            return;
        }
//        if (!mSettings.shouldShowDelayUpdateVersion() && mIsAutoUpdate) {
//            debugLog("下次提醒时间未到");
//            return;
//        }
        mNormalUpdateDialogImpl = new NormalUpdateDialogImpl(this, mContext, info, mBfcVersionDialogListener);
    }

    /**
     * 立即更新版本
     *
     * @param info 版本信息
     */
    void updateInstallNewVersion(VersionInfo info) {
        if (null == info) {
            mNormalUpdateDialogImpl.dismiss();
            return;
        }
        File file = info.getApkFile();
        if (file != null && file.exists()) {
            //  mBfcVersion.installApk(mContext, file);
            IntentUtils.startActivity(mContext, getInstallAppIntent(file));
        }
    }

    private Intent getInstallAppIntent(@NonNull File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String type;
        if (Build.VERSION.SDK_INT < 23) {
            type = "application/vnd.android.package-archive";
        } else {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileUtils.getFileExtension(file));
        }
        intent.setDataAndType(Uri.fromFile(file), type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * 延后更新版本
     */
    void updateLaterForThisVersion(VersionInfo info) {

//        if (mSettings.getSpIgnoreTimes() + 1 >= VersionSetting.MAXREMINDFIVEDAY) {
//            //mBfcVersion.ignoreVersion(info);
//            mNormalUpdateDialogImpl.dismiss();
//        } else {
        if (mIsAutoUpdate && mSettings != null) {
            mSettings.setDelayUpdateVersion();
        }
        if (mNormalUpdateDialogImpl != null) {
            mNormalUpdateDialogImpl.dismiss();
        }
//        }
    }

    public void destroy() {
        mBfcVersion.destroy();
        if (mNormalUpdateDialogImpl != null) {
            mNormalUpdateDialogImpl.dismiss();
            mNormalUpdateDialogImpl = null;
        }
        mProgressDialog = null;
        mSettings = null;
        mContext = null;
    }

    void onClickExit() {
        if(mVersionUIListener != null){
            mVersionUIListener.onExitApp();
        }
    }

    void onClickAgain() {
        DownloadController.getInstance().reloadTask(mBfcVersion.getAllDownloadTask().get(0));
        mProgressDialog.dismissBtn();
    }

    void setTag(boolean tag) {
        mTag = tag;
    }

    public interface VersionUIListener {
        void hasNewVersion(String versionCode);

        void onExitApp();
    }

    public void setVersionUIListener(VersionUIListener versionUIListener) {
        this.mVersionUIListener = versionUIListener;
    }

    public void setBfcVersionDialogListener(BfcVersionDialogListener listener) {
        mBfcVersionDialogListener = listener;
    }

    public void setBfcNotificationCallBack(BfcNotificationCallBack bfcNotificationCallBack) {
        mBfcNotificationCallBack = bfcNotificationCallBack;
    }

    public void setBfcVersionStateListener(BfcVersionStateListener l) {
        this.mBfcVersionStateListener = l;
    }

    public boolean isDownloading(){
        List<ITask> tasks = mBfcVersion.getAllDownloadTask();
        if(tasks == null || tasks.size() == 0){
            return false;
        }
        for (ITask task : tasks) {
            if(task == null){
                continue;
            }
            if(task.getState() == Status.DOWNLOAD_PROGRESS){
                return true;
            }
        }
        return false;
    }

    public List<ITask> getAllDownloadTask(){
        return mBfcVersion.getAllDownloadTask();
    }

    private void debugLog(String log) {
        if (mDebugLogListener != null) {
            mDebugLogListener.logStr(log);
        }
    }

    public interface DebugLogListener {
        void logStr(String str);
    }

    public void setDebugLogListener(DebugLogListener debugLogListener) {
        this.mDebugLogListener = debugLogListener;
    }

    private static class LeakHandler extends Handler {
        private final WeakReference<BfcVersionManager> mManagerWeakReference;

        LeakHandler(BfcVersionManager manager) {
            super(Looper.getMainLooper());
            mManagerWeakReference = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            BfcVersionManager versionManager = mManagerWeakReference.get();
            if (versionManager == null) {
                return;
            }
            if (msg.what == MSG_SHOW_SILENT_DIALOG) {
                LogUtils.i(TAG, "MSG_SHOW_SILENT_DIALOG");
                if(versionManager.mBfcVersionStateListener != null){
                    versionManager.mBfcVersionStateListener.onVersionState(BfcVersionStateListener.STATE_SILENT_READY);
                }
                new SilentUpdateDialog(versionManager.mContext, versionManager, (Version) msg.obj, versionManager.mBfcVersionDialogListener);
                versionManager.mIsAutoUpdate = false;
            } else if (msg.what == MSG_UPDATE_READY) {
                VersionInfo info = (VersionInfo) msg.obj;
                LogUtils.i(TAG, "MSG_UPDATE_READY mIsSilentUpdate:" + versionManager.mIsSilentUpdate + " versionManager.mHasDownload:" + versionManager.mHasDownload);
                if (versionManager.mIsSilentUpdate) {
                    if (versionManager.mHasDownload) {
                        if(versionManager.mBfcVersionStateListener != null){
                            versionManager.mBfcVersionStateListener.onVersionState(BfcVersionStateListener.STATE_SILENT_READY);
                        }
                        new SilentUpdateDialog(versionManager.mContext, info, versionManager, versionManager.mBfcVersionDialogListener);
                    } else {
                        versionManager.updateInstallNewVersion(info);
                        if(versionManager.mVersionUIListener != null){
                            versionManager.mVersionUIListener.onExitApp();
                        }
                    }
                    return;
                }
                if (versionManager.mHasDownload || !versionManager.mIsAutoUpdate) {
                    versionManager.showNormalUpdateDialog(info);
                }
            } else if (msg.what == MSG_PROGRESS_UPDATE) {
                if(versionManager.mProgressDialog != null){
                    versionManager.mProgressDialog.setProgressBar(msg.arg1);
                    versionManager.mProgressDialog.setProgressTV(msg.arg2 + "MB");
                    versionManager.mProgressDialog.setTipTV(msg.arg1 + "%");
                }
            } else if (msg.what == MSG_DOWNLOAD_MOBILE_DATA) {
                versionManager.mRequestDialog = new NetRequestDialogImpl(versionManager, versionManager.mContext, versionManager.mIsAutoUpdate, (ITask) msg.obj);
            } else if (msg.what == MSG_DOWNLOAD_PAUSE) {
                if(versionManager.mProgressDialog != null) {
                    versionManager.mProgressDialog.setTipTV(versionManager.mContext.getString(R.string.bfc_version_ui_download_pause));
                }
            } else if (msg.what == MSG_HAVE_NEW_VERSION) {
                if(versionManager.mVersionUIListener != null) {
                    versionManager.mVersionUIListener.hasNewVersion((String) msg.obj);
                }
            } else if (msg.what == MSG_DOWNLOAD_FAILED) {
                if(versionManager.mProgressDialog != null) {
                    versionManager.mProgressDialog.showBtn();
                    versionManager.mProgressDialog.setTipTV(versionManager.mContext.getString(R.string.bfc_version_ui_download_failed));
                }
                if (versionManager.mBfcNotificationCallBack != null) {
                    versionManager.mBfcNotificationCallBack.downloadFailed();
                }
            } else if (msg.what == MSG_DOWNLOAD_START) {
                if (versionManager.mRequestDialog != null) {
                    versionManager.mRequestDialog.dismiss();
                }
            }
        }
    }

    public BfcVersion getBfcVersion() {
        return mBfcVersion;
    }

    public static class Builder {
        private boolean isAutoUpdate = true;
        private boolean isDebug = false;
        private IUrl iUrl = new UrlReleaseImpl();

        public Builder() {
        }

        public Builder setIsAutoUpdate(boolean isAutoUpdate) {
            this.isAutoUpdate = isAutoUpdate;
            return this;
        }

        public Builder setIsDebug(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public Builder setIUrl(IUrl iUrl) {
            this.iUrl = iUrl;
            return this;
        }

        public BfcVersionManager build(Context context) {
            return new BfcVersionManager(context, isAutoUpdate, isDebug, iUrl);
        }
    }

}
