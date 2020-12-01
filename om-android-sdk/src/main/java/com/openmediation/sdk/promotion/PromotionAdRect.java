// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.promotion;

import java.util.HashMap;
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

    public static Map<String, Object> getExtraData(PromotionAdRect rect) {
        if (rect == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("width", rect.getWidth());
        data.put("height", rect.getHeight());
        data.put("scaleX", rect.getScaleX());
        data.put("scaleY", rect.getScaleY());
        data.put("angle", rect.getAngle());
        return data;
    }
}
