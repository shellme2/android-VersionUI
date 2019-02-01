package com.eebbk.tool.versionupdate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.eebbk.bfc.common.app.AppUtils;
import com.eebbk.bfc.common.devices.DeviceUtils;
import com.eebbk.bfc.sdk.version.entity.VersionInfo;

/**
 * 作者：haloQ
 * 实现的主要功能：
 * 创建日期：2017/3/31
 * 修改信息：
 */

class NormalUpdateDialogImpl {
    private AlertDialog mVersionUpdateAlertDialog;

    NormalUpdateDialogImpl(final BfcVersionManager manager, Context context, final VersionInfo info, final BfcVersionDialogListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.bfc_version_feature_show, null);
        TextView subTitleTv = (TextView) view.findViewById(R.id.tv_dialog_subhead);
        TextView appMessageTv = (TextView) view.findViewById(R.id.tv_dialog_message);
        subTitleTv.setText("“" + AppUtils.getAppName(context) + "”检测到新版本 V" + info.getRemoteVersionName());
        String introduce = info.getUpdateinformation();
        if (TextUtils.isEmpty(introduce) || TextUtils.equals(introduce, " ")) {
            introduce = context.getString(R.string.bfc_version_ui_dialog_default_msg);
        }
        appMessageTv.setMovementMethod(new ScrollingMovementMethod());
        appMessageTv.setText(introduce);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(R.string.bfc_version_ui_dialog_title);
        builder.setView(view);
//        builder.setCancelable(DeviceUtils.isPhone(context));
        builder.setPositiveButton(R.string.bfc_version_ui_update_now, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (listener != null) {
                    listener.onPositiveClick();
                }
                dismiss();
                manager.updateInstallNewVersion(info);
            }
        });
        builder.setNegativeButton(R.string.bfc_version_ui_update_later, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                manager.updateLaterForThisVersion(info);
                if (listener != null) {
                    listener.onNegativeClick();
                }
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0){
                    manager.updateLaterForThisVersion(info);
                    dismiss();
                    if (listener != null) {
                        listener.onNegativeClick();
                    }
                }
                return false;
            }
        });
        mVersionUpdateAlertDialog = builder.create();
        mVersionUpdateAlertDialog.show();

        mVersionUpdateAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                manager.updateLaterForThisVersion(info);
                if (listener != null) {
                    listener.onNegativeClick();
                }
            }
        });
    }

    void dismiss() {
        mVersionUpdateAlertDialog.dismiss();
    }
}
