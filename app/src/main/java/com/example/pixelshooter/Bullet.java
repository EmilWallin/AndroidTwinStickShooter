package com.example.pixelshooter;

import android.graphics.RectF;

public class Bullet {
    //size and location of bullet
    private RectF mRect;

    //Speed
    private float mXVelocity;
    private float mYVelocity;

    //size
    private float mWidth;
    private float mHeight;

    //Bullet belongs to 0 = player, 1 = enemy
    public static final int PLAYER_BULLET = 0;
    public static final int ENEMY_BULLET = 1;
    private int mBelongsTo;


    //Constructor
    public Bullet(int screenX){
        //Bullet initialized based on the screen x
        mWidth = screenX/100;
        mHeight = screenX/100;

        mRect = new RectF();
    }


    //Returns rectF reference
    public RectF getRect(){
        return mRect;
    }

    //Move bullet based on speed and framerate
    public void update(long fps){
        //Coordinates
        mRect.left = mRect.left + (mXVelocity / fps);
        mRect.top = mRect.top + (mYVelocity / fps);

        mRect.right = mRect.left + mWidth;
        mRect.bottom = mRect.top + mHeight;
    }

    //Spawn new bullet at pos
    void spawn(int belongsTo, int pX, int pY, float rotation, float shotSpeed){
        mBelongsTo = belongsTo;
        //Spawn bullet at location passed in
        mRect.left = pX;
        mRect.top = pY;
        mRect.right = mRect.left + mWidth;
        mRect.bottom = mRect.top + mHeight;

        //Head Away from the player in the rotation


        float velX = (float)Math.cos(Math.toRadians(rotation -90));
        float velY = (float)Math.sin(Math.toRadians(rotation -90));


        mXVelocity = velX * shotSpeed;
        mYVelocity = velY * shotSpeed;
    }

    public int getBelongsTo(){
        return mBelongsTo;
    }
}
