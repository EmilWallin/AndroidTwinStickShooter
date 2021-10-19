package com.example.pixelshooter;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.pixelshooter.Bullet.ENEMY_BULLET;
import static com.example.pixelshooter.Bullet.PLAYER_BULLET;

public class PixelShooterGame extends SurfaceView implements Runnable {

    private Activity activity;

    //Debugging
    private Boolean mDebugging = false;
    private String mPlayerMovementDebugString = "";
    private String mPlayerRotationDebugString = "";

    //Runnable fields
    private volatile boolean mPlaying;
    private Boolean mPaused = true;
    private Thread mGameThread = null;

    //ScreenResolution
    private int mScreenX;
    private int mScreenY;

    //Font Sizes
    private int mFontSize;
    private int mFontMargin;
    private int mSmallFontSize;
    //DebugFont
    private int mDebugFontSize;
    private int mDebugFontMargin;

    //Screen Drawing Objects
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    //Paints init
    //Paints
    private Paint mPaint = new Paint();
    //Current backgroundColor
    private int mBackgroundColor;

    //Framerate
    private long mFPS;
    private final int MILLIS_IN_SECOND = 1000;

    //Touch IDs
    private int moveID;
    private int rotateID;

    //GAME FIELDS
    private int mHighscore = 0;
    //Objects
    private Player mPlayer;
    private List<Enemy> mEnemies;
    private List<Bullet> mBullets;
    private List<Pillar> mPillars;

    //Score/Level/Health
    private int mLevel = 1;
    private String mLevelInfoString = "";
    private String mStartInfoString = "";

    private int mTouchCount = 0;
    //Movement joystick
    private JoyStick mMoveStick;
    private float mMoveCenterX;
    private float mMoveCenterY;
    private float mMoveX;
    private float mMoveY;
    private boolean mMoving = false;

    //Rotation joystick
    private JoyStick mRotateStick;
    private float mRotateCenterX;
    private float mRotateCenterY;
    private float mRotateX;
    private float mRotateY;
    private boolean mRotating = false;

    //Spawn Timer
    private long mStartGameTime;
    private long mSpawnDelay;
    private Timer mSpawnTimer;
    private int mEnemyLimit;
    private int mTotalEnemiesKilled = 0;
    private int[] mEnemiesPerLevel = {22, 50, 75};

    //GameOver/Cleared Levels
    private boolean mGameOver = false;
    private boolean mClearedLevel1 = false;
    private boolean mClearedLevel2 = false;
    private boolean mClearedLevel3 = false;

    //Sound stuff
    private SoundPool mSP;
    //Player sound ID
    private int mPlayerShootID = -1;
    private int mPlayerTakeDamageID = -1;
    //Enemy sound ID
    private int mSpawnEnemyID = -1;
    private int mEnemyShootID = -1;
    private int mEnemyTakeDamageID = -1;
    private int mEnemyDestroyedID = -1;
    //Bullet sound ID
    private int mBulletDestroyedID = -1;
    //Game ID
    private int mGameOverID = -1;
    private int mStartGameID = -1;

    //SoundPool sound level
    private float mVolume = 0.4f;

    //Constructor
    public PixelShooterGame(Context context, int x, int y){
        super(context);
        activity = (Activity)context;
        mScreenX = x;
        mScreenY = y;

        //MainFont
        mFontSize = mScreenX / 20;
        mFontMargin = mScreenY / 50;
        //SmallFont
        mSmallFontSize = mScreenX /30;
        //DebugFont
        mDebugFontSize = mScreenX / 40;
        mDebugFontMargin = mScreenY / 100;

        //Surface Init
        mSurfaceHolder = getHolder();

        mPlayer = new Player(this.getContext(), mScreenX, mScreenY);
        mEnemies = new ArrayList<Enemy>();
        mPillars = new ArrayList<Pillar>();
        mBullets = new ArrayList<Bullet>();

        //Joystick init
        mMoveStick = new JoyStick(mScreenX, mScreenY, mPaint);
        mRotateStick = new JoyStick(mScreenX, mScreenY, mPaint);

        initializeSoundPool();

        startGame();
    }

    //Initialize the soundpool and load all the sound IDs
    private void initializeSoundPool() {
        //Instantiate soundpool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Building audioattributes
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            //SoundPool
            mSP = new SoundPool.Builder().setMaxStreams(30).setAudioAttributes(audioAttributes).build();
        } else {
            mSP = new SoundPool(30, AudioManager.STREAM_MUSIC, 0);
        }

        //Open the files and load them into RAM
        try{
            AssetManager assetManager = this.getContext().getAssets();
            AssetFileDescriptor descriptor;
            //Player sound IDs
            descriptor = assetManager.openFd("playershoot.ogg");
            mPlayerShootID= mSP.load(descriptor, 0);
            descriptor = assetManager.openFd("playerdamaged.ogg");
            mPlayerTakeDamageID = mSP.load(descriptor, 0);
            //Enemy sound IDs
            descriptor = assetManager.openFd("enemyshoot.ogg");
            mEnemyShootID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd("enemyspawn.ogg");
            mSpawnEnemyID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd("enemydamaged.ogg");
            mEnemyTakeDamageID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd("enemykilled.ogg");
            mEnemyDestroyedID = mSP.load(descriptor, 0);
            //Game sound IDs
            descriptor = assetManager.openFd("gamestarted.ogg");
            mStartGameID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd("gameover.ogg");
            mGameOverID = mSP.load(descriptor, 0);

        } catch(IOException e){
            Log.d("error", "failed to load sound files");
        }
    }

    //Starts game, sets spawner and setups the level with pillars and paints background
    private void startGame() {
        setupLevel();

        //Set enemy spawn timer
        mSpawnTimer = new Timer();
        mSpawnTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                spawnEnemy();
            }
        }, 0, mSpawnDelay);
    }

    //Cleared level method
    private void clearedLevel() {
        calculateLevelBonus();

        switch(mLevel) {
            case 1:
                mClearedLevel1 = true;
                mLevel++;
                break;
            case 2:
                mClearedLevel2 = true;
                mLevel++;
                break;
            case 3:
                mClearedLevel3 = true;
                gameOver();
                return;
        }

        resetGame();
    }

    //Game over sets fields
    private void gameOver() {
        mSP.play(mGameOverID,mVolume,mVolume,0,0,1);
        mPaused = true;
        mGameOver = true;
    }

    //Calculate the level bonus
    private void calculateLevelBonus() {
        double pointsToAdd = mPlayer.getHealth() * mTotalEnemiesKilled * mLevel;
        pointsToAdd = pointsToAdd / ((System.currentTimeMillis() - mStartGameTime)/10000);
        //Cast to int. No decimals needed
        mHighscore += (int)pointsToAdd;
    }

    //Adds to mHighscore for every enemy killed
    private void addScoreOnEnemyKilled() {
        mHighscore += 25 * mLevel;
    }

    //Reset the game into starting position
    private void resetGame() {
        mSpawnTimer.cancel();
        mPlayer.resetCharacter(mScreenX, mScreenY);
        mEnemies.clear();
        mBullets.clear();
        mPaused = true;
        startGame();
    }

    //Setup level. fields which control spawndelays, background colors, and so on
    private void setupLevel() {
        switch(mLevel){
            case 1:
                mBackgroundColor = getResources().getColor(R.color.background1);
                spawnPillars(5);
                mSpawnDelay = 3000;
                mEnemyLimit = 8;
                mLevelInfoString = "Level 1";
                mStartInfoString = "Press to Start";
                break;
            case 2:
                mBackgroundColor = getResources().getColor(R.color.background2);
                spawnPillars(3);
                mSpawnDelay = 2500;
                mEnemyLimit = 9;
                mLevelInfoString = "Level 2";
                mStartInfoString = "Press to Start";
                break;
            case 3:
                mBackgroundColor = getResources().getColor(R.color.background3);
                spawnPillars(2);
                mSpawnDelay = 2000;
                mEnemyLimit = 10;
                mLevelInfoString = "Level 3";
                mStartInfoString = "Press to Start";
                break;
            default:
                mBackgroundColor = getResources().getColor(R.color.background3);
                mSpawnDelay = 2000;
                mEnemyLimit = 12;
                break;
        }
    }

    //Spawn pillars
    private void spawnPillars(int noOfPillars){
        mPillars.clear();
        for(int i = 0; i < 4; i++)
        {
            mPillars.add(new Pillar(this.getContext(), mScreenX, mLevel));
        }

        int pillarMargin = (int)(mPillars.get(mPillars.size() - 1).getRect().right - mPillars.get(mPillars.size() - 1).getRect().left);

        for (Pillar p : mPillars){
            //GET POSITION FOR X AND Y NOT DIRECTLY IN CENTER
            Random rndX = new Random();
            Random rndY = new Random();
            p.spawn(rndX.nextInt(mScreenX - pillarMargin*2) + pillarMargin, rndY.nextInt(mScreenY - pillarMargin*2) + pillarMargin);
        }
    }

    //Run - calls update, collision check, drawing, and so on. sets mFPS field
    @Override
    public void run() {
        while (mPlaying){
            long frameStartTime = System.currentTimeMillis();
            if(!mPaused){
                //Move everything, perform actions
                update();
                //Check collisions
                detectCollisions();
            }
            //Draw game
            draw();

            //Calculate fps
            long timeThisFrame = System.currentTimeMillis() - frameStartTime;
            if(timeThisFrame >= 1) {
                mFPS = MILLIS_IN_SECOND / timeThisFrame;
            }
        }
    }

    //Check collisions
    private void detectCollisions() {
        //Bullet collisions
        for (int i = 0; i < mBullets.size(); i++) {
            //Uses boolean to remove bullet in if-statement later
            boolean removeBullet = false;

            //Check collision with pillars
            for (Pillar p : mPillars) {
                if (mBullets.get(i).getRect().intersects(p.getRect().left, p.getRect().top, p.getRect().right, p.getRect().bottom)) {
                    removeBullet = true;
                }
            }

            //Check collision against enemies if the bullet belongs to player
            if (mBullets.get(i).getBelongsTo() == PLAYER_BULLET && !removeBullet) {
                for (int j = 0; j < mEnemies.size(); j++) {
                    if (mBullets.get(i).getRect().intersects(mEnemies.get(j).getRect().left, mEnemies.get(j).getRect().top, mEnemies.get(j).getRect().right, mEnemies.get(j).getRect().bottom)) {
                        if (mEnemies.get(j).loseHealth() <= 0) {
                            float[] volumes = getLeftRightVolume(mEnemies.get(j).getX());
                            mEnemies.remove(j);
                            mSP.play(mEnemyDestroyedID,volumes[0],volumes[1],0,0,1);
                            mTotalEnemiesKilled++;
                            addScoreOnEnemyKilled();
                        }
                        mSP.play(mEnemyTakeDamageID,mVolume,mVolume,0,0,1);
                        removeBullet = true;
                    }
                }
            }

            //Check collision against player if the bullet belongs to enemy
            if (mBullets.get(i).getBelongsTo() == ENEMY_BULLET && !removeBullet) {
                if (mBullets.get(i).getRect().intersects(mPlayer.getRect().left, mPlayer.getRect().top, mPlayer.getRect().right, mPlayer.getRect().bottom)) {
                    if (mPlayer.loseHealth() <= 0) {
                        gameOver();
                    }
                    removeBullet = true;
                    mSP.play(mPlayerTakeDamageID, mVolume, mVolume, 0,0,1);
                }
            }

            //Check collision against edges
            if (!removeBullet) {
                if(mBullets.get(i).getRect().left < 0 || mBullets.get(i).getRect().top < 0 || mBullets.get(i).getRect().right > mScreenX || mBullets.get(i).getRect().bottom > mScreenY) {
                    removeBullet = true;
                }
            }

            //if the bullet should be removed, there's no need to continue this cycle
            if (removeBullet){
                mBullets.remove(i);
                continue;
            }

            //Check collision with other bullets
            for (int k = 0; k < mBullets.size(); k++) {
                if (mBullets.get(i) == mBullets.get(k)){
                    continue;
                }
                if (mBullets.get(i).getRect().intersects(mBullets.get(k).getRect().left, mBullets.get(k).getRect().top, mBullets.get(k).getRect().right, mBullets.get(k).getRect().bottom)) {
                    mBullets.remove(k);
                    removeBullet = true;
                    break;
                }
            }
            //if the bullet should be removed, remove it, then it goes onto the next cycle
            if (removeBullet){
                mBullets.remove(i);
            }
        }

        int moveX = 0;
        int moveY = 0;
        //Player Collisions. Pillars, Enemies
        for (Pillar p : mPillars) {
            if (RectF.intersects(mPlayer.getRect(), p.getRect())) {
                float pillarX = p.getRect().centerX();
                float pillarY = p.getRect().centerY();
                float playerX = mPlayer.getRect().centerX();
                float playerY = mPlayer.getRect().centerY();

                float diffX = playerX - pillarX;
                float diffY = playerY - pillarY;

                if(diffX < 0) {
                    moveX = -10;
                } else {
                    moveX = 10;
                }
                if(diffY < 0){
                    moveY = -10;
                } else {
                    moveY = 10;
                }
            }
        }
        for (Enemy e : mEnemies) {
            if (RectF.intersects(mPlayer.getRect(), e.getRect())){
                float pillarX = e.getRect().centerX();
                float pillarY = e.getRect().centerY();
                float playerX = mPlayer.getRect().centerX();
                float playerY = mPlayer.getRect().centerY();

                float diffX = playerX - pillarX;
                float diffY = playerY - pillarY;

                if(diffX < 0) {
                    moveX = -10;
                } else {
                    moveX = 10;
                }
                if(diffY < 0){
                    moveY = -10;
                } else {
                    moveY = 10;
                }
            }
        }
        //Nudges player in the opposite direction of the pillar/enemy when colliding
        mPlayer.pushPlayer(moveX, moveY);
    }

    //Update positions
    private void update() {
        if(mTouchCount == 0) {
            mMoving = false;
            mRotating = false;
        }

        for (Bullet b : mBullets) {
            b.update(mFPS);
        }

        playerActions();
        enemyAction();

        //Check if enemy kill limit has been reached
        if(mTotalEnemiesKilled >= mEnemiesPerLevel[mLevel - 1]) {
            clearedLevel();
        }
    }

    //Player actions. Take input and applies it to the player
    private void playerActions() {
        //Moves player
        if (mMoving) {
            float movementX = mMoveX - mMoveCenterX;
            float movementY = mMoveY - mMoveCenterY;

            //Check to see that player doesnt go out of screen
            if (mPlayer.getRect().right > mScreenX - 5) {
                movementX = -10;
            } else if (mPlayer.getRect().left < 5) {
                movementX = 10;
            }
            if (mPlayer.getRect().top < 5) {
                movementY = 10;
            } else if (mPlayer.getRect().bottom > mScreenY - 5) {
                movementY = -10;
            }

            mPlayer.move(movementX, movementY, mFPS);
            mPlayerMovementDebugString = "Move X = " + movementX + ". Move Y = " + movementY;
        }

        //Rotates player
        if (mRotating) {
            float rotationX = mRotateX - mRotateCenterX;
            float rotationY = mRotateCenterY - mRotateY;
            mPlayer.rotate(rotationX, rotationY);
            mPlayerRotationDebugString = "Rot X = " + rotationX + ". Rot Y = " + rotationY;
            //Can the player shoot? if so, shoot
            if(mPlayer.getCanShoot()) {
                spawnBullet(PLAYER_BULLET, mPlayer);
                float[] volumes = getLeftRightVolume(mPlayer.getRect().centerX());
                mSP.play(mPlayerShootID,volumes[0],volumes[1],0,0,1);
            }
        }
    }

    //EnemyActions. Rotate/shoot
    private void enemyAction(){
        try{
            for(Enemy e : mEnemies) {
                e.rotate(mPlayer.getX(), mPlayer.getY(), mFPS);
                if(e.getCanShoot()) {
                    spawnBullet(ENEMY_BULLET, e);
                    float[] volumes = getLeftRightVolume(e.getRect().centerX());
                    mSP.play(mEnemyShootID,volumes[0],volumes[1],0,0,1);
                }
            }
        } catch (Exception ex) {
            Log.d("Error", "Could not loop through in enemy actions");
        }

    }

    //Gets left and right volumes depending on x-position
    private float[] getLeftRightVolume(float x) {
        float right = mVolume;
        float left = mVolume;

        float middle = mScreenX/2;

        right += x/middle;
        left -= x/middle;

        return new float[] {left,right};
    }

    //Spawns bullet, belonging to int value and spawns at IShootingEntitys position
    private void spawnBullet(int belongsTo, IShootingEntity shooter) {
        mBullets.add(new Bullet(mScreenX));
        int pX = (int)shooter.getX();
        int pY = (int)shooter.getY();
        mBullets.get(mBullets.size() -1).spawn(belongsTo, pX, pY, shooter.getRotation(), shooter.getShotSpeed());
        shooter.setCanShoot(false);
    }

    //Spawns enemy in world
     private void spawnEnemy() {
        if(!mPlaying || mPaused || mEnemies.size() > mEnemyLimit){
            return;
        }

        //Stop spawning enemies if all enemies of this level already spawned
        if (mTotalEnemiesKilled + mEnemies.size() >= mEnemiesPerLevel[mLevel -1]) {
            return;
        }

        //This section encountered problem before, hence the try/catch
        try {
            mSP.play(mSpawnEnemyID,mVolume,mVolume,0,0,1);
            Random rndX = new Random();
            Random rndY = new Random();

            mEnemies.add(new Enemy(this.getContext(), mScreenX, mScreenY, mLevel));
            boolean enemyCollidingWithPillar = false;

            //gets enemy size to spawn it on the screen later
            int enemySizeMargin = (int)(mEnemies.get(mEnemies.size() - 1).getRect().right - mEnemies.get(mEnemies.size() - 1).getRect().left);

            //Do-loop to check if the enemy spawned inside a pillar. will loop until it gets coordinates where the enemy is shootable
            do {
                int randX = rndX.nextInt(mScreenX - enemySizeMargin*2) + enemySizeMargin;
                int randY = rndY.nextInt(mScreenY - enemySizeMargin*2) + enemySizeMargin;
                mEnemies.get(mEnemies.size() - 1).spawn(randX, randY);

                for (Pillar p : mPillars) {
                    if (RectF.intersects(mEnemies.get(mEnemies.size() -1).getRect(),p.getRect())) {
                        enemyCollidingWithPillar = true;
                        break;
                    }
                }
            } while (enemyCollidingWithPillar);
        } catch (Exception ex) {
            Log.d("Error: ", "Couldn't spawn enemy, but still reached try: " + ex.getMessage());
        }
    }

    //Draw everything
    private void draw(){
        if(mSurfaceHolder.getSurface().isValid()){
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawColor(mBackgroundColor);
            mPaint.setColor(Color.argb(255,0,0,0));

            //DRAWING CODE
            //DRAW PILLARS
            for (Pillar p : mPillars) {
                mCanvas.drawBitmap(p.getBitmap(), p.getRect().left, p.getRect().top, mPaint);
            }

            //DRAW ENEMIES
            for (int i = 0; i < mEnemies.size(); i++){
                Matrix enemyMatrix = new Matrix();
                enemyMatrix.postRotate(mEnemies.get(i).getRotation(), mEnemies.get(i).getBitmap().getWidth()/2, mEnemies.get(i).getBitmap().getHeight()/2);
                enemyMatrix.postTranslate(mEnemies.get(i).getRect().left, mEnemies.get(i).getRect().top);
                mCanvas.drawBitmap(mEnemies.get(i).getBitmap(), enemyMatrix, mPaint);
                //Draw enemy hitboxes if debugging
                if(mDebugging) {
                    mCanvas.drawRect(mEnemies.get(i).getRect(), mPaint);
                }
            }

            //DRAW BULLETS
            for (Bullet b : mBullets) {
                mCanvas.drawRect(b.getRect(),mPaint);
            }

            //DRAW PLAYER
            Matrix playerMatrix = new Matrix();
            playerMatrix.postRotate(mPlayer.getRotation(), mPlayer.getBitmap().getWidth()/2, mPlayer.getBitmap().getHeight()/2);
            playerMatrix.postTranslate(mPlayer.getRect().left, mPlayer.getRect().top);
            mCanvas.drawBitmap(mPlayer.getBitmap(), playerMatrix, mPaint);

            //Player hitbox if debugging
            if (mDebugging) {
                mCanvas.drawRect(mPlayer.getRect(), mPaint);
            }

            //DRAW JOYSTICKS
            if(mMoving) {
                mCanvas.drawCircle(mMoveX, mMoveY, mMoveStick.getRadius(), mPaint);
                mCanvas.drawLine(mMoveCenterX, mMoveCenterY, mMoveX, mMoveY, mPaint);
            }
            if(mRotating) {
                mCanvas.drawCircle(mRotateX, mRotateY, mRotateStick.getRadius(), mPaint);
                mCanvas.drawLine(mRotateCenterX, mRotateCenterY, mRotateX, mRotateY, mPaint);
            }

            //DRAW UI
            mPaint.setTextSize(mSmallFontSize);
            //Health text
            mCanvas.drawText("Health: " + mPlayer.getHealth(), mScreenX/50, mFontMargin+mSmallFontSize, mPaint);

            //Enemies killed/Progress
            mCanvas.drawText("Enemies: " + mTotalEnemiesKilled + "/" + mEnemiesPerLevel[mLevel - 1], mScreenX/50, mFontMargin+mSmallFontSize + mScreenY/10, mPaint);
            //Timer
            mCanvas.drawText( "Time: " + (int)((System.currentTimeMillis() - mStartGameTime)/1000), mScreenX/20 * 17, mFontMargin+mSmallFontSize + mScreenY/10, mPaint);

            //Current score
            mPaint.setTextSize((float)(mSmallFontSize * 1.2));
            mCanvas.drawText("SCORE: " + mHighscore, (int)(mScreenX/20 *8.7), (float)(mFontMargin+mSmallFontSize*1.2), mPaint);

            mPaint.setTextSize(mSmallFontSize);
            //Level
            mCanvas.drawText("Level " + mLevel, mScreenX/20 * 17, mFontMargin+mSmallFontSize, mPaint);

            //GameInfo string
            if (mPaused  && !mGameOver )
            {
                mPaint.setTextSize(mFontSize);
                mCanvas.drawText(mLevelInfoString, (int)(mScreenX/2.3), mScreenY/2 - mFontSize, mPaint);
                mCanvas.drawText(mStartInfoString, (int)(mScreenX/2.8), mScreenY/2 + mFontSize, mPaint);

                mPaint.setTextSize((int)(mSmallFontSize*0.6));
                mCanvas.drawText("Press and drag here to move.", mScreenX/30, (mScreenY/5)*4, mPaint);
                mCanvas.drawText("Press and drag here to aim and shoot.", (mScreenX/20)*13, (mScreenY/5)*4, mPaint);
            }

            //GameOver screen
            if (mGameOver) {
                mPaint.setTextSize(mFontSize);
                mCanvas.drawText("Game Over", (int)(mScreenX/2.5), mScreenY/2 - mFontSize, mPaint);
                mCanvas.drawText("Final Score: " + mHighscore, (int)(mScreenX/3), mScreenY/2 + mFontSize, mPaint);
                mPaint.setTextSize((int)(mSmallFontSize*0.6));
                mCanvas.drawText("Press to go to the menu.", mScreenX/3, (mScreenY/5)*4, mPaint);
            }

            if(mDebugging){
                printDebuggingText();
            }

            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    //Touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Gets pointerindex and ID of the event
        int pointerIndex = event.getActionIndex();
        int pointerID = event.getPointerId(pointerIndex);

        //Switch for the different actions
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                //IF gameover, go back to main menu
                if (mGameOver) {
                    activity.finish();
                    return true;
                }
                //If paused => start game
                if (mPaused) {
                    mSP.play(mStartGameID,mVolume,mVolume,0,0,1);
                    mStartGameTime = System.currentTimeMillis();
                    mPaused = false;
                    mLevelInfoString = "";
                    mStartInfoString = "";
                }

                //If there are already two fingers on screen, ignore any more
                if (mTouchCount == 2) {
                    return true;
                }

                //Two if-statements two check which side of the screen has been pressed. Left side = movement. Right side = rotation.
                mTouchCount++;

                if (event.getX(pointerIndex) <= mScreenX / 2 && moveID == -1) {
                    moveID = pointerID;
                    mMoving = true;
                    mMoveCenterX = event.getX(pointerIndex);
                    mMoveCenterY = event.getY(pointerIndex);
                    mMoveX = event.getX(pointerIndex);
                    mMoveY = event.getY(pointerIndex);
                }
                if (event.getX(pointerIndex) > mScreenX / 2 && rotateID == -1) {
                    rotateID = pointerID;
                    mRotating = true;
                    mRotateCenterX = event.getX(pointerIndex);
                    mRotateCenterY = event.getY(pointerIndex);
                    mRotateX = event.getX(pointerIndex);
                    mRotateY = event.getY(pointerIndex);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //Move-event doesn't have an ID, therefore we need to retrieve it every time
                int pointerCount = event.getPointerCount();
                //Security check if there are two than more pointers
                if (pointerCount > 2) {
                    pointerCount = 2;
                }

                //Check pointerID for each pointer on the screen. Checks against move and rotation IDs and then does some calculations
                for (int i = 0; i < pointerCount; i++) {
                    pointerID = event.getPointerId(i);

                    if (pointerID == moveID) {
                        float displacement = (float) (Math.sqrt(Math.pow(event.getX(i) - mMoveCenterX, 2)) + Math.sqrt(Math.pow(event.getY(i) - mMoveCenterY, 2)));

                        if (displacement < mScreenX / 6) {
                            if (event.getX(i) > mScreenX / 2) {
                                mMoveX = mScreenX / 2;
                            } else {
                                mMoveX = event.getX(i);
                            }
                            mMoveY = event.getY(i);
                        } else {
                            float ratio = mScreenX / 6 / displacement;
                            float constrainedX = mMoveCenterX + (event.getX(i) - mMoveCenterX) * ratio;
                            float constrainedY = mMoveCenterY + (event.getY(i) - mMoveCenterY) * ratio;
                            mMoveX = constrainedX;
                            mMoveY = constrainedY;
                        }
                    }

                    if (pointerID == rotateID) {
                        float displacement = (float) (Math.sqrt(Math.pow(event.getX(i) - mRotateCenterX, 2)) + Math.sqrt(Math.pow(event.getY(i) - mRotateCenterY, 2)));

                        if (displacement < mScreenX / 6) {
                            if (event.getX(i) < mScreenX / 2) {
                                mRotateX = mScreenX / 2;
                            } else {
                                mRotateX = event.getX(i);
                            }
                            mRotateY = event.getY(i);
                        } else {
                            float ratio = mScreenX / 6 / displacement;
                            float constrainedX = mRotateCenterX + (event.getX(i) - mRotateCenterX) * ratio;
                            float constrainedY = mRotateCenterY + (event.getY(i) - mRotateCenterY) * ratio;
                            mRotateX = constrainedX;
                            mRotateY = constrainedY;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                //These have a pointerID belonging to the action
                if(mTouchCount > 0) {
                    mTouchCount--;
                }
                //De-activate movement
                if (pointerID == moveID) {
                    moveID = -1;
                    mMoving = false;
                    if (mTouchCount == 0) {
                        mRotating = false;
                    }
                }
                //De-activate rotation
                if (pointerID == rotateID) {
                    rotateID = -1;
                    mRotating = false;
                    if (mTouchCount == 0) {
                        mMoving = false;
                    }
                }
                break;
        }
        return true;
    }

    //Prints the debugging text
    private void printDebuggingText(){
        mPaint.setTextSize(mDebugFontSize);
        mCanvas.drawText("FPS: " + mFPS, 10, mDebugFontMargin+mDebugFontSize, mPaint);
        mCanvas.drawText(mPlayerMovementDebugString, 10, mDebugFontMargin+mDebugFontSize + 35, mPaint);
        mCanvas.drawText(mPlayerRotationDebugString, 10, mDebugFontMargin+mDebugFontSize + 70, mPaint);
        mCanvas.drawText("No of bullets: " + mBullets.size(), 10, mDebugFontMargin+mDebugFontSize + 95, mPaint);
    }

    //Resume and Pause Methods. Starts and joins threads, pauses game, and so on
    public void resume(){
        mPlaying = true;
        mStartInfoString = "Press to resume";
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    public void pause(){
        mPlaying = false;
        mPaused = true;
        try{
            mGameThread.join();
        } catch (InterruptedException e){
            Log.e("Error:", "joining thread");
        }
    }
}
