package com.damian.main;

public class Pose {
	private int imgLocation;
	private int x;
	private int y;
	private int xVel;
	private int yVel;
	private int duration;
	public Pose(int location, int x, int y, int xVel, int yVel, int duration){
		this.x = x;
		this.y = y;
		this.xVel = xVel;
		this.yVel = yVel;
		this.duration = duration;
		this.imgLocation = location;
	}
	public int getLocation(){
		return imgLocation;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public int getXVel(){
		return xVel;
	}
	public int getYVel(){
		return yVel;
	}
	public int getDuration(){
		return duration;
	}
}
