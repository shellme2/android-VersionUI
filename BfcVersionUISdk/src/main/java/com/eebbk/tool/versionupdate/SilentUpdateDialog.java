package com.eebbk.tool.versionupdate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.eebbk.bfc.common.app.AppUtils;
import com.eebbk.bfc.core.sdk.version.util.log.LogUtils;
import com.eebbk.bfc.sdk.version.entity.Version;
import com.eebbk.bfc.sdk.version.entity.VersionInfo;

/**
 * 作者：haloQ
 * 实现的主要功能：
 * 创建日期：2017/3/31
 * 修改信息：
 */

class SilentUpdateDialog {
    private static final String TAG = "SilentUpdateDialog";

    SilentUpdateDialog(Context context, final BfcVersionManager manager, final Version version, final BfcVersionDialogListener listener) {
        LogUtils.i(TAG, "SilentUpdateDialog - beginDownload");
        View view = LayoutInflater.from(context).inflate(R.layout.bfc_version_silent_update_dialog, null);
        TextView subTitleTv = (TextView) view.findViewById(R.id.tv_dialog_subhead);
        TextView appSizeTv = (TextView) view.findViewById(R.id.tv_dialog_app_size);
        TextView appMessageTv = (TextView) view.findViewById(R.id.tv_dialog_app_message);
        subTitleTv.setText("“" + AppUtils.getAppName(context) + "”检测到新版本 V" + version.getVersionName());
        appSizeTv.setText("大小：" + manager.getKbToMb(version.getFileSize()));
        String introduce = version.getUpdateinformation();
        if (TextUtils.isEmpty(introduce) || TextUtils.equals(introduce, " ")) {
            introduce = context.getString(R.string.bfc_version_ui_dialog_default_msg);
        }
        appMessageTv.setMovementMethod(new ScrollingMovementMethod());
        appMessageTv.setText(introduce);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(R.string.bfc_version_ui_dialog_title);
        builder.setCancelable(false);
        builder.setView(view);
        builder.setPositiveButton(R.string.bfc_version_ui_update_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                LogUtils.i(TAG, "立即更新-beginDownload");
                manager.beginDownload(version);
                manager.beginProgress(version);
                if (listener != null) {
                    listener.onPositiveClick();
                }
            }
        });
        builder.create().show();
    }

    SilentUpdateDialog(Context context, final VersionInfo info, final BfcVersionManager manager, final BfcVersionDialogListener listener) {
        LogUtils.i(TAG, "SilentUpdateDialog - install");
        View view = LayoutInflater.from(context).inflate(R.layout.bfc_version_silent_update_dialog, null);
        TextView subTitleTv = (TextView) view.findViewById(R.id.tv_dialog_subhead);
        TextView appSizeTv = (TextView) view.findViewById(R.id.tv_dialog_app_size);
        TextView appMessageTv = (TextView) view.findViewById(R.id.tv_dialog_app_message);
        subTitleTv.setText("“" + AppUtils.getAppName(context) + "”检测到新版本 V" + info.getRemoteVersionName());
        appSizeTv.setText("大小：" + manager.getKbToMb(info.getApkFile().length()));
        String introduce = info.getUpdateinformation();
        if (TextUtils.isEmpty(introduce) || TextUtils.equals(introduce, " ")) {
            introduce = context.getString(R.string.bfc_version_ui_dialog_default_msg);
        }
        appMessageTv.setMovementMethod(new ScrollingMovementMethod());
        appMessageTv.setText(introduce);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(R.string.bfc_version_ui_dialog_title);
        builder.setCancelable(false);
        builder.setView(view);
        builder.setPositiveButton(R.string.bfc_version_ui_update_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                LogUtils.i(TAG, "立即更新-install");
                manager.updateInstallNewVersion(info);
                manager.onClickExit();
                if (listener != null) {
                    listener.onPositiveClick();
                }
            }
        });
        builder.create().show();
    }
}
