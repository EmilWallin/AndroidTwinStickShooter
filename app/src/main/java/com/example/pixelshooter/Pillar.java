package com.example.pixelshooter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class Pillar {
    //size and location of bullet
    private RectF mRect;

    //size
    private float mWidth;
    private float mHeight;

    private Bitmap mBitmap;

    //Constructor
    public Pillar(Context context, int screenX, int level){
        //Pillar initialized based on the screen x
        mWidth = screenX/20;
        mHeight = screenX/20;

        mRect = new RectF();

        //Prepare bitmap
        switch(level) {
            case 1:
                mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pillar1);
                break;
            case 2:
                mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pillar2);
                break;
            case 3:
                mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pillar3);
                break;
        }
        mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)mWidth, (int)mHeight, false);
    }

    //Returns rectF reference
    public RectF getRect(){
        return mRect;
    }
    //Returns bitmap reference
    public Bitmap getBitmap() {
        return mBitmap;
    }

    //Spawn pillar at position pX and pY
    void spawn(int pX, int pY){
        //Spawn bullet at location passed in
        mRect.left = pX;
        mRect.top = pY;
        mRect.right = mRect.left + mWidth;
        mRect.bottom = mRect.top + mHeight;
    }
}
