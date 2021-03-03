// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.openmediation.sdk.utils.DensityUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DrawCrossMarkView extends View {

    //line 1's increment on axis-X
    private int line1X = 0;

    //line 1's increment on axis-Y
    private int line1Y = 0;

    //line 2's increment on axis-X
    private int line2X = 0;

    //line 2's increment on axis-X
    private int line2Y = 0;

    //origin of cross
    float line1StartX;
    float line2StartX;
    float lineStartY;

    //line's max increment
    int maxLineIncrement;

    //line's thickness
    private int lineThick = 6;

    //arc center's x
    float center;

    //arc's radius
    float radius;

    //arc's shape and boundaries
    RectF rectF;

    Paint paint;

    //control's width
    float totalWidth;

    int color;

    public DrawCrossMarkView(Context context) {
        super(context);
        totalWidth = DensityUtil.dip2px(context, 20);
        maxLineIncrement = (int) (totalWidth * 2 / 5);
        this.color = Color.WHITE;
        init();
    }

    public DrawCrossMarkView(Context context, int color) {
        super(context);
        totalWidth = DensityUtil.dip2px(context, 20);
        maxLineIncrement = (int) (totalWidth * 2 / 5);
        this.color = color;
        init();
    }

    public DrawCrossMarkView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Pattern p = Pattern.compile("\\d*");

        Matcher m = p.matcher(attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_width"));

        if (m.find()) {
            totalWidth = Float.valueOf(m.group());
        }

        totalWidth = DensityUtil.dip2px(context, totalWidth);

        maxLineIncrement = (int) (totalWidth * 2 / 5);

        init();
    }

    public DrawCrossMarkView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    void init() {
        paint = new Paint();
        //
        paint.setColor(color);

        //
        paint.setStrokeWidth(lineThick);

        //
        paint.setStyle(Paint.Style.STROKE);

        //
        paint.setAntiAlias(true);

        //sets center's x
        center = (totalWidth / 2);

        //sets radius
        radius = (totalWidth / 2) - lineThick;

        //sets cross's origin
        line1StartX = (center + totalWidth / 5);
        lineStartY = (center - totalWidth / 5);
        line2StartX = (center - totalWidth / 5);

        rectF = new RectF(center - radius,
                center - radius,
                center + radius,
                center + radius);

        setClickable(true);
    }

    //the drawinf function
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draws arc
        canvas.drawArc(rectF, 235, -360, false, paint);
        //draws cross
        line1X = maxLineIncrement;
        line1Y = maxLineIncrement;
        //draws line 1
        canvas.drawLine(line1StartX, lineStartY, line1StartX - line1X, lineStartY + line1Y, paint);
        line2X = maxLineIncrement;
        line2Y = maxLineIncrement;
        //draws line 2
        canvas.drawLine(line2StartX, lineStartY, line2StartX + line2X, lineStartY + line2Y, paint);
    }

}