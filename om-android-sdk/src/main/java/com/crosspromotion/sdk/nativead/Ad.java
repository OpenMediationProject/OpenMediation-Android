// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.nativead;

import android.graphics.Bitmap;

public final class Ad {
    private final String mTitle;
    private final String mDescription;
    private final String mCTA;
    private final Bitmap mContent;
    private final Bitmap mIcon;

    private Ad(Builder builder) {
        this.mTitle = builder.mTitle;
        this.mDescription = builder.mDescription;
        this.mCTA = builder.mCTA;
        this.mContent = builder.mContent;
        this.mIcon = builder.mIcon;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getCTA() {
        return mCTA;
    }

    public Bitmap getContent() {
        return mContent;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public static class Builder {
        private String mTitle;
        private String mDescription;
        private String mCTA;
        private Bitmap mContent;
        private Bitmap mIcon;

        public Builder title(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder description(String des) {
            this.mDescription = des;
            return this;
        }

        public Builder cta(String cta) {
            this.mCTA = cta;
            return this;
        }

        public Builder content(Bitmap bitmap) {
            this.mContent = bitmap;
            return this;
        }

        public Builder icon(Bitmap icon) {
            this.mIcon = icon;
            return this;
        }

        public Ad build() {
            return new Ad(this);
        }
    }
}
