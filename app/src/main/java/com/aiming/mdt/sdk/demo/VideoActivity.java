package com.aiming.mdt.sdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aiming.mdt.video.VideoAd;
import com.aiming.mdt.video.VideoAdListener;


/**
 * Created by xuxq on 18/3/23.
 */
public class VideoActivity extends Activity {

    private final static String TAG = "VideoActivity";
    private TextView showMstTv, title;
    //private Handler mHandler;
    private VideoAd videoAd;
    //广告加载标识：0，加载中，1，加载成功，2，加载失败
    private int loadAdFlag = -1;
    private String placementId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.act_video);
        loadAdFlag = -1;
        //mHandler = new Handler();
        showMstTv = this.findViewById(R.id.show_load_msg);
        title = this.findViewById(R.id.title);
        placementId = this.getIntent().getStringExtra("placementId");
        if (TextUtils.isEmpty(placementId)) {
            placementId = "1122";
        }
        title.setText("VideoAd--placementId:" + placementId);
        videoAd = new VideoAd(this, placementId, new VideoAdListener() {
            @Override
            public void onADReady() {
                Log.d(TAG, "--VideoAd ready placementId:" + placementId);
                loadAdFlag = 1;
                showMstTv.setText("VideoAd Ready placementId:" + placementId);
            }

            @Override
            public void onADClick() {
                Log.d(TAG, "--VideoAd click placementId:" + placementId);
                showMstTv.setText("VideoAd Click placementId:" + placementId);
            }

            @Override
            public void onADFail(String msg) {
                loadAdFlag = 2;
                final String failMsg = String.format("VideoAd Fail : %s", msg);
                Log.d(TAG, failMsg);
                showMstTv.setText(failMsg);
            }

            @Override
            public void onADFinish(boolean isFullyWatched) {
                Log.d(TAG, "--VideoAd finish placementId:" + placementId);
                showMstTv.setText("VideoAd Finish placementId:" + placementId);
            }
        });
        videoAd.setExtId("111");
    }

    public void goBack(View view) {
        finish();
    }

    public void loadVideoAd(View view) {
        showMstTv.setText("VideoAd Loading");
        loadAdFlag = 0;
        videoAd.loadAd();
    }

    public void showVideoAd(View view) {
        if (loadAdFlag == 0) {
            showMstTv.setText("VideoAd 加载中");
        } else if (loadAdFlag == 1) {
            if (videoAd != null && videoAd.isReady()) {
                videoAd.show();
            } else {
                showMstTv.setText("VideoAd Not Ready");
            }
        } else if (loadAdFlag == 2) {
            showMstTv.setText("VideoAd Loading");
            loadAdFlag = 0;
            videoAd.loadAd();
        }
    }

    @Override
    public void onDestroy() {
        if (videoAd != null) {
            videoAd.destroy();
        }
        super.onDestroy();
    }
}
