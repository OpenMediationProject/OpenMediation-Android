package com.openmediation.sdk.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.openmediation.sdk.demo.utils.Constants;
import com.openmediation.sdk.demo.view.ILoadMoreListener;
import com.openmediation.sdk.demo.view.LoadMoreRecyclerView;
import com.openmediation.sdk.demo.view.LoadMoreView;
import com.openmediation.sdk.nativead.AdIconView;
import com.openmediation.sdk.nativead.AdInfo;
import com.openmediation.sdk.nativead.MediaView;
import com.openmediation.sdk.nativead.NativeAd;
import com.openmediation.sdk.nativead.NativeAdListener;
import com.openmediation.sdk.nativead.NativeAdView;
import com.openmediation.sdk.utils.error.Error;

import java.util.ArrayList;
import java.util.List;

public class NativeRecyclerActivity extends Activity {
    private static final String TAG = "NativeRecyclerActivity";

    private static final int LIST_ITEM_COUNT = 10;
    private LoadMoreRecyclerView mRecyclerView;
    private MyAdapter myAdapter;
    private List<AdInfo> mData;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_recycler);
        NativeAd.addAdListener(Constants.P_NATIVE, mNativeAdListener);
        initListView();
    }

    private void initListView() {
        mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.my_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mData = new ArrayList<>();
        myAdapter = new MyAdapter(this, mData);
        mRecyclerView.setAdapter(myAdapter);
        mRecyclerView.setLoadMoreListener(new ILoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.d(TAG, "------onLoadMore------");
                loadNativeAd();
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadNativeAd();
            }
        }, 500);
    }

    private final NativeAdListener mNativeAdListener = new NativeAdListener() {
        @Override
        public void onNativeAdLoaded(String placementId, AdInfo info) {
            Log.d(TAG, "onNativeAdLoaded, placementId: " + placementId + ", AdInfo : " + info);
            loadSuccess(info);
        }

        @Override
        public void onNativeAdLoadFailed(String placementId, Error error) {
            Log.d(TAG, "onNativeAdLoadFailed, placementId: " + placementId + ", error : " + error);
            loadFinish();
            loadSuccess(null);
        }

        @Override
        public void onNativeAdImpression(String placementId, AdInfo info) {
            Log.d(TAG, "onNativeAdImpression, placementId: " + placementId + ", info : " + info);
        }

        @Override
        public void onNativeAdClicked(String placementId, AdInfo info) {
            Log.d(TAG, "onNativeAdClicked, placementId: " + placementId + ", info : " + info);
        }
    };

    private void loadFinish() {
        if (mRecyclerView != null) {
            mRecyclerView.setLoadingFinish();
        }
    }

    private void loadSuccess(AdInfo info) {
        loadFinish();
        for (int i = 0; i < LIST_ITEM_COUNT; i++) {
            mData.add(null);
        }
        int count = mData.size();
        int random = (int) (Math.random() * LIST_ITEM_COUNT) + count - LIST_ITEM_COUNT;
        mData.set(random, info);
        myAdapter.notifyDataSetChanged();
    }

    private void loadNativeAd() {
        // for TikTok and TencentAd in China traffic
        NativeAd.setDisplayParams(Constants.P_NATIVE, 320, 0);
        NativeAd.loadAd(Constants.P_NATIVE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NativeAd.removeAdListener(Constants.P_NATIVE, mNativeAdListener);
        for (AdInfo info : mData) {
            if (info != null) {
                NativeAd.destroy(Constants.P_NATIVE, info);
            }
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    private static class MyAdapter extends RecyclerView.Adapter {
        private static final int FOOTER_VIEW_COUNT = 1;

        private static final int ITEM_VIEW_TYPE_LOAD_MORE = 1;
        private static final int ITEM_VIEW_TYPE_NORMAL = 2;
        private static final int ITEM_VIEW_TYPE_AD = 3;

        private List<AdInfo> mData;
        private Context mContext;

        public MyAdapter(Context context, List<AdInfo> data) {
            this.mContext = context;
            this.mData = data;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_VIEW_TYPE_LOAD_MORE:
                    return new LoadMoreViewHolder(new LoadMoreView(mContext));
                case ITEM_VIEW_TYPE_AD:
                    return new NativeAdViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_nativead, parent, false));
                default:
                    return new NormalViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_normal, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof NativeAdViewHolder) {
                holder.itemView.setBackgroundColor(Color.WHITE);
                NativeAdViewHolder adViewHolder = (NativeAdViewHolder) holder;
                adViewHolder.setData(Constants.P_NATIVE, mData.get(position));
            } else if (holder instanceof NormalViewHolder) {
                NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
                normalViewHolder.textView.setText("Recycler Item " + position);
                holder.itemView.setBackgroundColor(getColorRandom());
            } else if (holder instanceof LoadMoreViewHolder) {
                LoadMoreViewHolder loadMoreViewHolder = (LoadMoreViewHolder) holder;
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        private int getColorRandom() {
            int a = Double.valueOf(Math.random() * 255).intValue();
            int r = Double.valueOf(Math.random() * 255).intValue();
            int g = Double.valueOf(Math.random() * 255).intValue();
            int b = Double.valueOf(Math.random() * 255).intValue();
            return Color.argb(a, r, g, b);
        }

        @Override
        public int getItemCount() {
            int count = mData == null ? 0 : mData.size();
            return count + FOOTER_VIEW_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (mData != null) {
                int count = mData.size();
                if (position >= count) {
                    return ITEM_VIEW_TYPE_LOAD_MORE;
                } else {
                    AdInfo ad = mData.get(position);
                    if (ad == null) {
                        return ITEM_VIEW_TYPE_NORMAL;
                    } else {
                        return ITEM_VIEW_TYPE_AD;
                    }
                }
            }
            return super.getItemViewType(position);
        }

        /**
         * NativeAdViewHolder
         */
        private static class NativeAdViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout itemContainView;
            private Context mContext;

            public NativeAdViewHolder(View itemView) {
                super(itemView);
                mContext = itemView.getContext();
                itemContainView = itemView.findViewById(R.id.native_ad_container);
            }

            public void setData(String placementId, AdInfo info) {
                if (info == null) {
                    return;
                }
                itemContainView.removeAllViews();
                if (info.isTemplateRender()) {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(Gravity.CENTER);
                    itemContainView.addView(info.getView(), layoutParams);
                } else {
                    View adView = LayoutInflater.from(mContext).inflate(R.layout.native_ad_layout, null);
                    TextView title = adView.findViewById(R.id.ad_title);
                    title.setText(info.getTitle());
                    TextView desc = adView.findViewById(R.id.ad_desc);
                    desc.setText(info.getDesc());
                    Button btn = adView.findViewById(R.id.ad_btn);
                    btn.setText(info.getCallToActionText());
                    MediaView mediaView = adView.findViewById(R.id.ad_media);
                    NativeAdView nativeAdView = new NativeAdView(mContext);
                    AdIconView adIconView = adView.findViewById(R.id.ad_icon_media);
                    nativeAdView.addView(adView);
                    nativeAdView.setTitleView(title);
                    nativeAdView.setDescView(desc);
                    nativeAdView.setAdIconView(adIconView);
                    nativeAdView.setCallToActionView(btn);
                    nativeAdView.setMediaView(mediaView);

                    NativeAd.registerNativeAdView(placementId, nativeAdView, info);

                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    itemContainView.addView(nativeAdView, layoutParams);
                }
            }
        }

        private static class NormalViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public NormalViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.text_idle);
            }
        }

        /**
         * LoadMore ViewHolder
         */
        private static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
            TextView mTextView;
            ProgressBar mProgressBar;

            public LoadMoreViewHolder(View itemView) {
                super(itemView);

                itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

                mTextView = (TextView) itemView.findViewById(R.id.tv_load_more_tip);
                mProgressBar = (ProgressBar) itemView.findViewById(R.id.pb_load_more_progress);
            }
        }
    }

}
