package com.aiming.mdt.sdk.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aiming.mdt.nativead.AdIconView;
import com.aiming.mdt.nativead.AdInfo;
import com.aiming.mdt.nativead.MediaView;
import com.aiming.mdt.nativead.NativeAd;
import com.aiming.mdt.nativead.NativeAdListener;
import com.aiming.mdt.nativead.NativeAdView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by xuxq on 18/3/23.
 */
public class NativeActivity extends Activity {

    private final static String TAG = "NativeActivity";
    //图片最大宽高
    private static final int maxWidth = 768;
    private static final int maxHeight = 1024;
    private TextView showMstTv, title;
    private RelativeLayout adParent;
    private NativeAd nativeAd;
    private NativeAdView nativeAdView;
    private AdInfo mAdInfo;
    private View adView;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_native);
        nativeAdView = new NativeAdView(this);
        showMstTv = this.findViewById(R.id.show_load_msg);
        adParent = this.findViewById(R.id.native_ad_container);
        title = this.findViewById(R.id.title);
        String placementId = this.getIntent().getStringExtra("placementId");
        if (TextUtils.isEmpty(placementId)) {
            placementId = "1097";
        }
        title.setText("NativeAd--placementId:" + placementId);
        nativeAd = new NativeAd(this, placementId, new NativeAdListener() {
            @Override
            public void onADReady(AdInfo adInfo) {
                Log.d(TAG, "---nativeAD is ready--" + adInfo.toString());
                mAdInfo = adInfo;
                showMstTv.setText("nativeAD Ready");

                isLoading = false;
            }

            @Override
            public void onADClick() {
                Log.d(TAG, "---nativeAD is click--");
                showMstTv.setText("nativeAD Click");
            }

            @Override
            public void onADFail(String msg) {
                final String showMsg = String.format("nativeAD Fail : %s", msg);
                Log.d(TAG, showMsg);

                showMstTv.setText(showMsg);
                isLoading = false;
            }
        });
    }

    public void goBack(View view) {
        finish();
    }

    public void loadNativeAd(View view) {
        isLoading = true;
        mAdInfo = null;
        adParent.removeAllViews();
        showMstTv.setText("nativeAD Loading");
        nativeAd.loadAd();
    }

    public void showNativeAd(View view) {
        if (isLoading) {
            Toast.makeText(this, "ad is loading", Toast.LENGTH_SHORT).show();
            return;
        }
        if ((nativeAdView != null) && (mAdInfo != null)) {
            nativeAdView.removeAllViews();
            adView = View.inflate(this, R.layout.native_ad_layout, null);
            TextView title = adView.findViewById(R.id.ad_title);
            title.setText(mAdInfo.getTitle());
            TextView text = adView.findViewById(R.id.ad_text);
            text.setText(mAdInfo.getDesc());
            Button btn = adView.findViewById(R.id.ad_btn);
            btn.setText(mAdInfo.getCallToActionText());

            MediaView mediaView = adView.findViewById(R.id.ad_media);

            AdIconView iconMediaView = adView.findViewById(R.id.ad_icon_media);

            adParent.removeAllViews();
            nativeAdView.addView(adView);


            nativeAdView.setTitleView(title);
            nativeAdView.setDescView(text);
            nativeAdView.setAdIconView(iconMediaView);
            nativeAdView.setMediaView(mediaView);
            nativeAdView.setCallToActionView(btn);

            nativeAd.registerNativeAdView(nativeAdView);

            adView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            adView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.addRule(Gravity.CENTER);
            adParent.addView(nativeAdView, layoutParams);
        } else {
            Toast.makeText(this, "-native ad 加载中--", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        super.onDestroy();
    }

    private void downloadBitmap(final String url, final ImageView imageView) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //请求参数拼接
                    HttpURLConnection con = null;
                    try {
                        Log.d(TAG, "start downloadBitmap");
                        URL u = new URL(url);
                        con = (HttpURLConnection) u.openConnection();
                        con.setDoInput(true);
                        con.setUseCaches(true);
                        con.setConnectTimeout(30000);
                        con.setReadTimeout(60000);
                        con.connect();

                        if (con.getResponseCode() == 200) {
                            InputStream in = con.getInputStream();
                            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                            int size;
                            byte[] buf = new byte[4096];
                            while ((size = in.read(buf)) != -1) {
                                out.write(buf, 0, size);
                            }
                            out.flush();
                            in.close();
                            byte[] body = out.toByteArray();

                            final Bitmap bitmap = BitmapFactory.decodeByteArray(body, 0, body.length);

                            if (bitmap == null) {
                                Log.d(TAG, "downloadBitmap response error : empty bitmap");
                                return;
                            } else {
                                Log.d(TAG, "downloadBitmap success");
                            }
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(bitmap);
                                    Log.d(TAG, "setImageBitmap success url :" + url);
                                }
                            });

                        } else {
                            Log.d(TAG, "downloadBitmap response error :" + con.getResponseCode() + " " + con.getResponseMessage());
                        }
                    } catch (final Exception e) {
                        Log.d(TAG, "downloadBitmap response error :" + e.toString());
                    } finally {
                        if (con != null) {
                            con.disconnect();
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.d(TAG, "downloadBitmap error :" + e.toString());
        }
    }
}
