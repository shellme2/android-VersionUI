package com.eebbk.tool.versionupdate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * 作者：haloQ
 * 实现的主要功能：
 * 创建日期：2017/3/31
 * 修改信息：
 */

public class NetRequestDialogImpl {
    private AlertDialog mAlertDialog;

    public NetRequestDialogImpl(final BfcVersionManager manager, Context context, final boolean isSilentUpdate, final ITask iTask) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        builder.setMessage(R.string.bfc_version_ui_net_request_dialog_msg);
        builder.setPositiveButton(R.string.bfc_version_ui_continue_btn_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                manager.useMobile(iTask);
                manager.setTag(true);

            }
        });

        String negativeTxt;
        if (isSilentUpdate) {
            negativeTxt = context.getString(R.string.bfc_version_ui_exit);
            builder.setCancelable(false);
        } else {
            negativeTxt = context.getString(R.string.bfc_version_ui_cancel);
            builder.setCancelable(true);
        }
        builder.setNegativeButton(negativeTxt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                manager.setTag(true);
                if (isSilentUpdate) {
                    manager.onClickExit();
                } else {
                    mAlertDialog.dismiss();
                }
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    void dismiss() {
        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }


}
