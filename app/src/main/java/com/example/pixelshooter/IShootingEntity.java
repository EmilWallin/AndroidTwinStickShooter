package com.example.pixelshooter;

//Interface for things that can shoot
public interface IShootingEntity {
    public float getX();
    public float getY();
    public float getRotation();
    public boolean getCanShoot();
    public void setCanShoot(boolean canShoot);
    public float getShotSpeed();
    public int loseHealth();
}
