package com.openmediation.sdk.banner;

public enum AdSize {

    /**
     *
     */
    BANNER(320, 50),
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
