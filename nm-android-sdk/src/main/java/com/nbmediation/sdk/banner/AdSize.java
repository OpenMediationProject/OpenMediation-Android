package com.nbmediation.sdk.banner;

public enum AdSize {

    /**
     *
     */
    BANNER(320, 50),
    AD_SIZE_320X50(320, 50),
    AD_SIZE_320X100(320, 100),
    AD_SIZE_300X250(300, 250),
    LEADERBOARD(728, 90),
    MEDIUM_RECTANGLE(300, 250);

    int width;
    int height;

    private AdSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
