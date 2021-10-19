package com.example.pixelshooter;

import android.graphics.Paint;

public class JoyStick {
    private Paint mPaint;
    private float mRadius;


    public JoyStick(int x, int y, Paint paint) {
        mRadius = x / 50;
        mPaint = paint;
    }

    public float getRadius() {
        return mRadius;
    }
    public Paint getPaint() {
        return mPaint;
    }
}
