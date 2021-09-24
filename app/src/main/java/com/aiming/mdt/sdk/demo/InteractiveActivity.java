package com.aiming.mdt.sdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aiming.mdt.interactive.InteractiveAd;
import com.aiming.mdt.interactive.InteractiveAdListener;

public class InteractiveActivity extends Activity {
    private final static String TAG = "appwallActivity";
    private TextView showMstTv;
    private InteractiveAd interactiveAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_appwall);
        TextView title = this.findViewById(R.id.title);
        showMstTv = findViewById(R.id.show_load_msg);
        String placementId = this.getIntent().getStringExtra("placementId");
        if (TextUtils.isEmpty(placementId)) {
            placementId = "2872";
        }
        title.setText("interactiveAd--placementId:" + placementId);
        interactiveAd = new InteractiveAd(this, placementId, new InteractiveAdListener() {
            @Override
            public void onADReady() {
                Log.d(TAG, "---interactiveAd-- ad ready");
                showMstTv.setText("interactiveAd Ready");
            }

            @Override
            public void onADFail(String msg) {
                final String showMsg = String.format("interactiveAd Fail : %s", msg);
                Log.d(TAG, showMsg);
                showMstTv.setText(showMsg);
            }

            @Override
            public void onADClose() {
                final String showMsg = String.format("interactiveAd close");
                Log.d(TAG, showMsg);
                showMstTv.setText(showMsg);
            }
        });
    }

    public void goBack(View view) {
        finish();
    }

    public void loadAppwallAd(View view) {
        showMstTv.setText("interactiveAd Loading");
        interactiveAd.loadAd();
    }

    public void showAppwallAd(View view) {
        if (interactiveAd.isReady()) {
            interactiveAd.show();
        } else {
            showMstTv.setText("not ready");
        }
    }

    @Override
    public void onDestroy() {
        if (interactiveAd != null) {
            interactiveAd.destroy();
        }
        super.onDestroy();
    }
}
