package com.example.pixelshooter;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Timer;
import java.util.TimerTask;

public class Player implements IShootingEntity {

    private Bitmap mBitmap;

    private RectF mRect;
    private float mPlayerHeight;
    private float mPlayerWidth;

    //Game fields
    //is moving
    private boolean mCanMove = true;

    //Shot fields
    private int mShotOffset;
    private float mRotation = 0;

    //Speed
    private float mVelocityX;
    private float mVelocityY;

    //ShotTimer
    private Timer mShotTimer;
    private long mShotCooldown = 250;
    private boolean canShoot = false;

    //Shotspeed
    private float mShotSpeed = 500;

    //Health
    private int mHealth = 8;

    public Player(Context context, float screenX, float screenY) {
        mPlayerHeight = screenY /12;
        mPlayerWidth = (float)(mPlayerHeight*0.8);

        mVelocityX = screenX/950;
        mVelocityY = screenX/950;

        mRect = new RectF(screenX/2, screenY/2, (screenX/2) + mPlayerWidth, (screenY/2) + mPlayerHeight);

        //Prepare bitmap
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)mPlayerWidth, (int)mPlayerHeight, false);

        mShotTimer = new Timer();

        mShotTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                canShoot = true;
            }
        }, 0, mShotCooldown);
    }

    void move(float moveX, float moveY, long fps) {
        moveX = (float)(moveX * mVelocityX /fps);
        moveY = (float)(moveY * mVelocityY /fps);

        if(!mCanMove){
            return;
        }
        mRect.left += moveX;
        mRect.top += moveY;
        mRect.bottom += moveY;
        mRect.right += moveX;
    }


    //Push player away from collider
    public void pushPlayer(int x, int y) {
        mCanMove = false;
        mRect.left += x;
        mRect.right += x;

        mRect.top += y;
        mRect.bottom += y;
        mCanMove = true;
    }

    //Rotate player
    public void rotate(float rotateX, float rotateY) {
        mRotation = (float)Math.toDegrees(Math.atan2(rotateX, rotateY));
    }

    public void resetCharacter(int screenX, int screenY){
        mRect.left = screenX/2;
        mRect.top = screenY/2;
        mRect.bottom = (screenY/2) + mPlayerHeight;
        mRect.right = (screenX/2) + mPlayerWidth;

        mHealth = 8;
    }

    public int loseHealth() {
        return --mHealth;
    }
    public int getHealth() {
        return mHealth;
    }


    //Get rotation, position
    @Override
    public float getX() {
        return mRect.centerX();
    }
    @Override
    public float getY() {
        return mRect.centerY();
    }
    @Override
    public float getRotation() {
        return mRotation;
    }

    //Get bitmap
    Bitmap getBitmap(){
        return mBitmap;
    }

    //Get, set rect
    RectF getRect(){
        return mRect;
    }
    public void setRect(RectF playerRect) {
        playerRect = mRect;
    }

    public boolean getCanMove() {
        return mCanMove;
    }
    public void setCanMove(boolean canMove) {
        mCanMove = canMove;
    }

    public void setCanShoot(boolean canShoot) {
        this.canShoot = canShoot;
    }
    @Override
    public boolean getCanShoot() {
        return canShoot;
    }
    @Override
    public float getShotSpeed() {
        return mShotSpeed;
    }


}
