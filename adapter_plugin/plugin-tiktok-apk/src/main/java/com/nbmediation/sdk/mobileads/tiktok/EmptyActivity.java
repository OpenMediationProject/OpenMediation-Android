package com.nbmediation.sdk.mobileads.tiktok;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.nbmediation.sdk.mobileads.PluginApplication;


/**
 * Created by jiantao.tu on 2020/6/18.
 */
public class EmptyActivity extends Activity {


    private static final String TAG = "EmptyActivity";
    private static TTRewardVideoAd mRewardedVideoAd;

    private static TTFullScreenVideoAd mFullScreenVideoAd;

    public final static int SHOW_REWARD_VIDEO = 1;

    public final static int SHOW_INTERSTITIAL = 2;

    public final static String SHOW_TYPE = "show_type";

    public static void showRewardVideoAd(TTRewardVideoAd rewardedVideoAd) {
        mRewardedVideoAd = rewardedVideoAd;
        Intent intent = new Intent(PluginApplication.getInstance(), EmptyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SHOW_TYPE, SHOW_REWARD_VIDEO);
        PluginApplication.getInstance().startActivity(intent);
    }

    public static void showInterstitialAd(TTFullScreenVideoAd fullScreenVideoAd) {
        mFullScreenVideoAd = fullScreenVideoAd;
        Intent intent = new Intent(PluginApplication.getInstance(), EmptyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SHOW_TYPE, SHOW_INTERSTITIAL);
        PluginApplication.getInstance().startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(TAG, "EmptyActivity onCreate");
//        Toast.makeText(this, "EmptyActivity起来了", Toast.LENGTH_SHORT).show();
        int showType = getIntent().getIntExtra(SHOW_TYPE, 0);
        if (showType == SHOW_REWARD_VIDEO && mRewardedVideoAd != null) {
            mRewardedVideoAd.showRewardVideoAd(this);
        } else if (showType == SHOW_INTERSTITIAL && mFullScreenVideoAd != null) {
            mFullScreenVideoAd.showFullScreenVideoAd(this);
        } else {
            finish();
            return;
        }
        PluginApplication.MAIN_HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
        mRewardedVideoAd = null;
        mFullScreenVideoAd = null;
        Log.i(TAG, "EmptyActivity onStop");
    }
}
