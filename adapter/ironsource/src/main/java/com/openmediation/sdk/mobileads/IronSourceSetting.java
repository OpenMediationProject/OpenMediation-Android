package com.openmediation.sdk.mobileads;

public class IronSourceSetting {

    private static boolean mMediationMode = false;

    /**
     * Use ironSource Mediation API.
     * Must to called before SDK initialization.
     */
    public static void setMediationMode(boolean mediation) {
        mMediationMode = mediation;
    }

    public static boolean isMediationMode() {
        return mMediationMode;
    }
}
