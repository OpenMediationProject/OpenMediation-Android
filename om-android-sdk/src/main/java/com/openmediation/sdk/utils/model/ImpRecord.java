// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import java.util.List;
import java.util.Map;

public final class ImpRecord {

    private Map<String, Map<String, Imp>> mImpMap;

    private Map<String, List<DayImp>> mDayImp;

    public Map<String, Map<String, Imp>> getImpMap() {
        return mImpMap;
    }

    public void setImpMap(Map<String, Map<String, Imp>> impMap) {
        this.mImpMap = impMap;
    }

    public void setDayImp(Map<String, List<DayImp>> dayImp) {
        this.mDayImp = dayImp;
    }

    public Map<String, List<DayImp>> getDayImp() {
        return mDayImp;
    }

    @Override
    public String toString() {
        return "ImpRecord{" +
                "mImpMap=" + mImpMap +
                '}';
    }

    public static class DayImp {
        //Impression time
        private String mTime;
        //Impression count
        private int mImpCount;

        public String getTime() {
            return mTime;
        }

        public void setTime(String time) {
            this.mTime = time;
        }

        public int getImpCount() {
            return mImpCount;
        }

        public void setImpCount(int impCount) {
            this.mImpCount = impCount;
        }

        @Override
        public String toString() {
            return "DayImp{" +
                    "mTime='" + mTime + '\'' +
                    ", mImpCount=" + mImpCount +
                    '}';
        }
    }

    public static class Imp extends DayImp {

        private String mPlacementId;

        //Package name
        private String mPkgName;
        //Last Impression Time
        private long mLastImpTime;

        public String getPlacmentId() {
            return mPlacementId;
        }

        public void setPlacmentId(String placementId) {
            this.mPlacementId = placementId;
        }

        public void setPkgName(String pkgName) {
            this.mPkgName = pkgName;
        }

        public String getPkgName() {
            return mPkgName;
        }

        public long getLashImpTime() {
            return mLastImpTime;
        }

        public void setLashImpTime(long lashImpTime) {
            this.mLastImpTime = lashImpTime;
        }

        @Override
        public String toString() {
            return "Imp{" +
                    "mPlacementId='" + mPlacementId + '\'' +
                    ", mPkgName='" + mPkgName + '\'' +
                    ", mLastImpTime=" + mLastImpTime +
                    '}';
        }
    }
}
