// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.openmediation.sdk.utils.DensityUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.crash.CrashUtil;


/**
 *
 */
public class AdMarketView extends RelativeLayout {
    private static String TAG = "AdMarketView";
    private String mActUrl;

    public AdMarketView(Context context, Bitmap logo, String act) {
        super(context);
        mActUrl = act;

        ImageView adLogoImgView = new ImageView(getContext());
        addView(adLogoImgView);

        int size = DensityUtil.dip2px(getContext(), 15);
        adLogoImgView.getLayoutParams().width = size;
        adLogoImgView.getLayoutParams().height = size;
        adLogoImgView.setImageBitmap(logo);

        adLogoImgView.bringToFront();


        adLogoImgView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri url = Uri.parse(mActUrl);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(url);
                    getContext().startActivity(intent);
                } catch (Throwable e) {
                    DeveloperLog.LogD(TAG, e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        });
    }
}
