// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.promotion;

import java.util.Map;

public class PromotionAdRect {
    private int width;
    private int height;
    private float scaleX;
    private float scaleY;
    private float angle;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public static PromotionAdRect getAdRect(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        PromotionAdRect rect = new PromotionAdRect();
        Object width = data.get("width");
        try {
            rect.setWidth(Integer.parseInt(String.valueOf(width)));
        } catch (Exception ignored) {
        }
        Object height = data.get("height");
        try {
            rect.setHeight(Integer.parseInt(String.valueOf(height)));
        } catch (Exception ignored) {
        }
        Object scaleX = data.get("scaleX");
        try {
            rect.setScaleX(Float.parseFloat(String.valueOf(scaleX)));
        } catch (Exception ignored) {
        }
        Object scaleY = data.get("scaleY");
        try {
            rect.setScaleY(Float.parseFloat(String.valueOf(scaleY)));
        } catch (Exception ignored) {
        }
        Object angle = data.get("angle");
        try {
            rect.setAngle(Float.parseFloat(String.valueOf(angle)));
        } catch (Exception ignored) {
        }
        return rect;
    }
}
