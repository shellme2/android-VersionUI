package com.eebbk.tool.versionupdate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 作者：haloQ
 * 实现的主要功能：
 * 创建日期：2017/3/30
 * 修改信息：
 */

class VersionProgressDialogImpl {

    private AlertDialog mProgressDialog;
    private ProgressBar mProgressBar;
    private TextView mProgressTV;
    private TextView mTotalSizeTV;
    private TextView mTipTV;
    private View mDivider;
    private Button mExitBtn;
    private Button mAgainBtn;
    private BfcVersionManager mManager;

    VersionProgressDialogImpl(Context context, BfcVersionManager manager) {
        mManager = manager;
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.bfc_version_progress_dialog_layout, null);
        mProgressTV = (TextView) view.findViewById(R.id.version_progress_tv);
        mTotalSizeTV = (TextView) view.findViewById(R.id.version_total_size_tv);
        mTipTV = (TextView) view.findViewById(R.id.version_progress_tip_tv);
        mDivider =  view.findViewById(R.id.version_vertical_divider);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mExitBtn = (Button) view.findViewById(R.id.version_exit_btn);
        mAgainBtn = (Button) view.findViewById(R.id.version_skip_btn);
        mExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.onClickExit();
            }
        });
        mAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.onClickAgain();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        builder.setCancelable(false);
        builder.setView(view);
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mManager.onClickExit();
                }
                return false;
            }
        });
        mProgressDialog = builder.create();
    }

    void dismissBtn() {
        mAgainBtn.setVisibility(View.GONE);
        mExitBtn.setVisibility(View.GONE);
        mDivider.setVisibility(View.GONE);
    }

    void showBtn() {
        mAgainBtn.setVisibility(View.VISIBLE);
        mExitBtn.setVisibility(View.VISIBLE);
        mDivider.setVisibility(View.VISIBLE);
    }

    void show() {
        mProgressDialog.show();
    }

    void dismiss() {
        mProgressDialog.dismiss();
    }

    void setProgressBar(int progress) {
        mProgressBar.setProgress(progress);
    }
    void setProgressTV(String progress){
        mProgressTV.setText(progress);
    }

    void setTipTV(String tip){
        mTipTV.setText(tip);
    }

    void setToTalSizeTV(String size){
        mTotalSizeTV.setText(size);
    }

}
