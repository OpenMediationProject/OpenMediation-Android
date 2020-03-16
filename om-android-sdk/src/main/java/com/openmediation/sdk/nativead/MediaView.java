// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.nativead;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Native ads content display wrapper, developer should provide an MediaView adapt image content or video content
 *
 * 
 */
public class MediaView extends RelativeLayout {
    public MediaView(Context context) {
        super(context);
    }

    public MediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
