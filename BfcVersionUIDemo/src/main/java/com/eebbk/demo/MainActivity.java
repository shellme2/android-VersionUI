package com.eebbk.demo;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eebbk.bfc.common.app.ToastUtils;
import com.eebbk.bfc.core.sdk.version.url.UrlTestImpl;
import com.eebbk.bfc.sdk.version.entity.VersionInfo;
import com.eebbk.tool.versionupdate.BfcVersionManager;
import com.eebbk.tool.versionupdate.BfcVersionStateListener;

public class MainActivity extends Activity {

    private BfcVersionManager mBfcVersionManager;
    private TextView mTextView1;
    private TextView  mTextView2;
    private TextView  mTextView;
    private RelativeLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView1 = (TextView) findViewById(R.id.textView1);
        mTextView2 = (TextView) findViewById(R.id.textView2);
        mTextView = (TextView) findViewById(R.id.info_tv);
        mLayout = (RelativeLayout) findViewById(R.id.layout);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                check();
//            }
//        }).start();

      //   mBfcVersionManager.showNormalUpdateDialog(new VersionInfo(this, 1, "1.1.1", 2, "沙发适当发生地方防守打法适当发生地方发沙发适当发生地方防守打法适当发生地方发生发生的发生沙发适当发生地方防守打法适当发生地方发生发生的发生沙发适当发生地方防守打法适当发生地方发生发生的发生沙发适当发生地方防守打法适当发生地方发生发生的发生生发生的发生", null, 1, "1.0.0", "", "", 2));

        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            mTextView1.setText("当前V" + pi.versionName);
            mTextView.setText("当前版本为V" + pi.versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void check() {
        mBfcVersionManager = new BfcVersionManager.Builder()
                .setIsAutoUpdate(true)
                .setIsDebug(false)
                .setIUrl(new UrlTestImpl())
                .build(this);
        mBfcVersionManager.setDebugLogListener(new BfcVersionManager.DebugLogListener() {
            @Override
            public void logStr(final String str) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.append("\n" + str);
                    }
                });
            }
        });
        mBfcVersionManager.setVersionUIListener(new BfcVersionManager.VersionUIListener() {
            @Override
            public void hasNewVersion(String code) {
                mTextView2.setText("发现新版本V" + code);
            }

            @Override
            public void onExitApp() {
                finish();
            }
        });
        mBfcVersionManager.setBfcVersionStateListener(new BfcVersionStateListener() {
            @Override
            public void onVersionState(int state) {

            }
        });
        mBfcVersionManager.onVersionCheck();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBfcVersionManager.setAutoUpdate(false);
                mBfcVersionManager.onVersionCheck();
            }
        });

        findViewById(R.id.button_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.getInstance(getApplicationContext()).l(R.string.test_tip);
            }
        });

        mBfcVersionManager.showNormalUpdateDialog(new VersionInfo(this,1,"1.1.1",2,"如题。看纪录片时想到的问题\n如题。看纪录片时想到的问题\n如题。看纪录片时想到的问题\n如题。看纪录片时想到的问题",null,1,"1.0.0","","",2));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBfcVersionManager.destroy();
    }
}
