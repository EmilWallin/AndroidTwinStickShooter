package com.example.pixelshooter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Timer;
import java.util.TimerTask;

public class Enemy implements IShootingEntity{
    //Enemy bitmap
    private Bitmap mBitmap;

    //Size/rect
    private RectF mRect;
    private float mEnemyHeight;
    private float mEnemyWidth;
    private float mRotation = 0;

    //Shooting fields
    private int mShotOffset;
    private float mShootDirectionX;
    private float mShootDirectionY;

    //Health
    private int mHealth;

    //ShotTimer
    private Timer mShotTimer;
    private long mShotCooldown = 900;
    private boolean canShoot = false;
    //Shotspeed
    private float mShotSpeed = 180;

    public Enemy(Context context, float screenX, float screenY, int level) {
        mEnemyHeight = screenY /14;
        mEnemyWidth = mEnemyHeight;

        mRect = new RectF(screenX/2, screenY/2, (screenX/2) + mEnemyWidth, (screenY/2) + mEnemyHeight);

        //Prepare bitmap
        switch(level) {
            case 1:
                mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy1);
                mShotSpeed = 180;
                mShotCooldown = 900;
                mHealth = 1;
                break;
            case 2:
                mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy2);
                mShotSpeed = 190;
                mShotCooldown = 800;
                mHealth = 2;
                break;
            case 3:
                mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy3);
                mShotSpeed = 205;
                mShotCooldown = 700;
                mHealth = 2;
                break;
            default:

                break;
        }
        mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)mEnemyWidth, (int)mEnemyHeight, false);

        mShotTimer = new Timer();

        mShotTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                canShoot = true;
            }
        }, 0, mShotCooldown);
    }

    void rotate(float playerX, float playerY, long fps){
        mRotation = (float) Math.toDegrees(Math.atan2(playerX - mRect.centerX(), mRect.centerY() - playerY));
    }

    void spawn(float pX, float pY) {
        mRect.left = pX - mEnemyWidth/2;
        mRect.right = mRect.left + mEnemyWidth;

        mRect.top = pY - mEnemyHeight/2;
        mRect.bottom = mRect.top + mEnemyHeight;
    }

    //Get rect
    RectF getRect(){
        return mRect;
    }

    //Get bitmap
    Bitmap getBitmap(){
        return mBitmap;
    }

    //Health methods
    public int getHealth() {
        return mHealth;
    }
    public void setHealth(int mHealth) {
        this.mHealth = mHealth;
    }
    public int loseHealth(){
        return --mHealth;
    }

    public boolean getCanShoot() {
        return canShoot;
    }
    public void setCanShoot(boolean canShoot) {
        this.canShoot = canShoot;
    }

    @Override
    public float getShotSpeed() {
        return mShotSpeed;
    }
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
}
