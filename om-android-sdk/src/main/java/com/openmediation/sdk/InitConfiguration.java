package com.openmediation.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InitConfiguration {
    private String mAppKey;
    private String mInitHost;
    private String mChannel;
    private boolean isLogEnable;
    private List<OmAds.AD_TYPE> mAdTypes;
    private List<OmAds.CACHE_TYPE> mCacheAdTypes;

    private InitConfiguration(Builder builder) {
        mAppKey = builder.appKey;
        mInitHost = builder.initHost;
        mChannel = builder.channel;
        isLogEnable = builder.logEnable;
        mAdTypes = builder.mPreloadAdTypes;
        mCacheAdTypes = builder.mUseCacheAdTypes;
    }

    public String getAppKey() {
        return mAppKey;
    }

    public String getInitHost() {
        return mInitHost;
    }

    public String getChannel() {
        return mChannel;
    }

    public boolean isLogEnable() {
        return isLogEnable;
    }

    public List<OmAds.AD_TYPE> getAdTypes() {
        return mAdTypes;
    }

    public List<OmAds.CACHE_TYPE> getCacheAdTypes() {
        return mCacheAdTypes;
    }

    public static class Builder {
        private String appKey;
        private String initHost;
        private String channel;
        private boolean logEnable;
        private List<OmAds.AD_TYPE> mPreloadAdTypes;
        private List<OmAds.CACHE_TYPE> mUseCacheAdTypes;

        public Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder initHost(String initHost) {
            this.initHost = initHost;
            return this;
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder logEnable(boolean enable) {
            this.logEnable = enable;
            return this;
        }

        public Builder preloadAdTypes(OmAds.AD_TYPE... adTypes) {
            this.mPreloadAdTypes = new ArrayList<>();
            this.mPreloadAdTypes.addAll(Arrays.asList(adTypes));
            return this;
        }

        public Builder useCacheAdTypes(OmAds.CACHE_TYPE... adTypes) {
            this.mUseCacheAdTypes = new ArrayList<>();
            this.mUseCacheAdTypes.addAll(Arrays.asList(adTypes));
            return this;
        }

        public InitConfiguration build() {
            return new InitConfiguration(this);
        }
    }
}
