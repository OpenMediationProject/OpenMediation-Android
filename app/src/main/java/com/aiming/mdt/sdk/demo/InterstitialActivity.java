package com.aiming.mdt.sdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aiming.mdt.interstitial.InterstitialAd;
import com.aiming.mdt.interstitial.InterstitialAdListener;


/**
 * Created by xuxq on 18/3/23.
 */

public class InterstitialActivity extends Activity {

    private final static String TAG = "InterstitialActivity";
    private TextView showMstTv, title;
    //private Handler mHandler;
    private InterstitialAd interstitialAd;
    //广告加载标识：0，加载中，1，加载成功，2，加载失败
    private int loadAdFlag = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_interstitial);
        showMstTv = this.findViewById(R.id.show_load_msg);
        //mHandler = new Handler();
        loadAdFlag = -1;
        title = this.findViewById(R.id.title);
        String placementId = this.getIntent().getStringExtra("placementId");
        if (TextUtils.isEmpty(placementId)) {
            placementId = "1099";
        }
        title.setText("interstitialAd--placementId:" + placementId);
        interstitialAd = new InterstitialAd(this, placementId, new InterstitialAdListener() {
            @Override
            public void onADReady() {
                Log.d(TAG, "--interstitialAd ready");
                //interstitialAd.show(DemoActivity.this);
                loadAdFlag = 1;
                showMstTv.setText("interstitialAd Ready");
            }

            @Override
            public void onADClick() {
                Log.d(TAG, "--interstitialAd click");
                showMstTv.setText("interstitialAd Click");
            }

            @Override
            public void onADFail(String msg) {
                loadAdFlag = 2;
                final String failMsg = String.format("interstitialAd Fail : %s", msg);
                Log.d(TAG, failMsg);
                showMstTv.setText(failMsg);
            }

            @Override
            public void onADClose() {
                Log.d(TAG, "--interstitialAd close");
                showMstTv.setText("interstitialAd Close");
            }
        });
    }


    public void goBack(View view) {
        finish();
    }

    public void loadInterstitialAd(View view) {
        showMstTv.setText("interstitialAd Loading");
        loadAdFlag = 0;
        interstitialAd.loadAd();
    }

    public void showInterstitialAd(View view) {
        if (loadAdFlag == 0) {
            showMstTv.setText("interstitialAd 正在加载");
        } else if (loadAdFlag == 1) {
            interstitialAd.show();
        } else if (loadAdFlag == 2) {
            showMstTv.setText("interstitialAd Loading");
            loadAdFlag = 0;
            interstitialAd.loadAd();
        }
    }

    @Override
    public void onDestroy() {
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
        super.onDestroy();
    }
}
