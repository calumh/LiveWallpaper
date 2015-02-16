package com.damian.main;

import java.io.IOException;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LiveWallpaper extends WallpaperService
{

	public static final String	SHARED_PREFS_NAME	= "livewallpapersettings";

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine()
	{
		return new TestPatternEngine();
	}

	class TestPatternEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener
	{

		private final Handler		mHandler		= new Handler();
		private float				mTouchX			= -1;
		private float				mTouchY			= -1;
		private final Paint			mPaint			= new Paint();
		private final Runnable		mDrawPattern	= new Runnable()
													{
														public void run()
														{	
															//Update after settings change
															if(backPreLocation.equals(mPreferences.getString("backLoc","black"))==false||imgPreLocation.equals(mPreferences.getString("imgLoc", "Oliver"))==false){
																imgPreLocation = mPreferences.getString("imgLoc", "Oliver");
																backPreLocation = mPreferences.getString("backLoc", "black");
																loadImages();
																//load background
																Bitmap backgroundTemp = null;
																System.out.println("Updated");
																background = null;
																
																final BitmapFactory.Options options = new BitmapFactory.Options();
																// Calculate inSampleSize
//																options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

																// Decode bitmap with inSampleSize set
																options.inJustDecodeBounds = false;
																options.inPreferredConfig = Config.RGB_565;
																options.inDither = true;
																if(mPreferences.getString("backLoc", "black").equals("black")){
																}else{
																	backgroundTemp = BitmapFactory.decodeFile(mPreferences.getString("backLoc", "black"),options);
																}
																
																if(backgroundTemp!=null){
																	final int canvasHeight = initYw;
	
																	int imageWidth = backgroundTemp.getWidth();
																	int imageHeight = backgroundTemp.getHeight();
																	
																	float scaleFactor = (float)canvasHeight/imageHeight;
	//																			Math.min( (float)canvasWidth / imageWidth, 
	//																	                              (float)canvasHeight / imageHeight );
																	Bitmap scaled = Bitmap.createScaledBitmap(  backgroundTemp, 
																	                                            (int)(scaleFactor * imageWidth), 
																	                                            (int)(scaleFactor * imageHeight), 
																	                                            true );
																	background = scaled;
																}
															}
															drawFrame();
														}
													};
		private boolean				mVisible;
		private SharedPreferences	mPreferences;
		
		
		//New Stuff
		private Bitmap[] imgFrames;

		private int x;
		private int y;
		private int x2;
		private int y2;
		private int initX;
		private int initY;
		private int initYw;
		private int preX;
		private int preY;
		private int velX;
		private int velY;
		private AssetManager man;
		private int currentFrame;
		boolean first;
		private Pose[] action;
		private boolean left;
		private boolean touched;
		private boolean paused;
		private boolean preInAir;
		private String imgPreLocation;
		private String backPreLocation;
		private SharedPreferences.Editor mEditor;
		private Bitmap background;
		private int delayTimer;
		private int delayChoose;
		private String choice;
		private String floorChoice;
		int bounceTimer = 0;
		boolean fallingDisabled;
		int statBarHeight;
		TestPatternEngine()
		{
			final Paint paint = mPaint;
			paint.setColor(0xffffffff);
			paint.setAntiAlias(true);
			paint.setStrokeWidth(2);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStyle(Paint.Style.STROKE);
			mPreferences = LiveWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
			mPreferences.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(mPreferences, null);
			mEditor = mPreferences.edit();
			
		}

		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key)
		{
	
//			mShape = prefs.getString("livewallpaper_testpattern", "smpte");
//			readColors();
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);
			man = getAssets();
			loadImages();
			currentFrame=0;
			x=200;
			y=880;
			x2= 200;
			y2 = 880;
			first = true;
			dash();
			left=true;
			imgPreLocation="Oliver";
			backPreLocation="black";
//			try {
//				background = BitmapFactory.decodeStream(man.open("Back/IMG_0618.JPG"));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			delayTimer = 0;
			delayChoose =0;
			preInAir=false;
			choice = "left";
			floorChoice = "stay";
			fallingDisabled=false;
			statBarHeight = getStatusBarHeight();
			System.out.println(getStatusBarHeight());
		}
		public int getStatusBarHeight() {
//		    int result = 0;
//		    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//		   
//		    if (resourceId > 0) {
//		        result = getResources().getDimensionPixelSize(resourceId);
//		    } 
//		    return mPreferences.getInt("stat", 9001)==9001 ? result-48 : mPreferences.getInt("stat", 9001)-48;
			return  mPreferences.getInt("stat", 200)-48;
		}
		@Override
		public void onDestroy()
		{
			super.onDestroy();
			mHandler.removeCallbacks(mDrawPattern);
		}

		@Override
		public void onVisibilityChanged(boolean visible)
		{
			mVisible = visible;
			if (visible)
			{				
				drawFrame();
			}
			else
			{
				mHandler.removeCallbacks(mDrawPattern);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height)
		{
			super.onSurfaceChanged(holder, format, width, height);

//			initFrameParams();

			drawFrame();
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder)
		{
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder)
		{
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mDrawPattern);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep,
				float yStep, int xPixels, int yPixels)
		{

			drawFrame();
		}

		/*
		 * Store the position of the touch event so we can use it for drawing
		 * later
		 */
		@Override
		public void onTouchEvent(MotionEvent event)
		{

			mTouchX = event.getX();
			mTouchY = event.getY();
			    
			int action = event.getAction();
			if(mTouchX>=x-20&&mTouchX<=x+128&&mTouchY>=y-20&&mTouchY<=y+128){
				switch(action){
					case MotionEvent.ACTION_DOWN:
						touched=true;
						break;
					case MotionEvent.ACTION_MOVE:
						touched=true;
						break;
					case MotionEvent.ACTION_UP:
						touched=false;
						break;
					case MotionEvent.ACTION_CANCEL:
						touched=false;
						break;
					case MotionEvent.ACTION_OUTSIDE:
						touched=false;
						break;
					default:
				}
			}else{
				touched=false;
			}
			
		}

		/*
		 * Draw one frame of the animation. This method gets called repeatedly
		 * by posting a delayed Runnable. You can do any drawing you want in
		 * here. This example draws a wireframe cube.
		 */
		void drawFrame()
		{
			final SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			try
			{	
				c = holder.lockCanvas();
				if (c != null)
				{							
					if(first){
						
						initX=c.getWidth();
						initY=c.getHeight()-(c.getHeight()/4);
						initYw = c.getHeight();
						x2=initX/2;
						y2=initY;
						x=x2;
						y=y2;
						first=false;
						
					}statBarHeight = getStatusBarHeight();
					initY = mPreferences.getInt("bot", -400) == -400 ? (c.getHeight()-(c.getHeight()/4)) : mPreferences.getInt("bot", -400);
//					initY = (c.getHeight()-(c.getHeight()/4))+mPreferences.getInt("bot", 0);
					// draw something
//					c.clipRect(x, y, x+128, y+128);
					drawPattern(c);
					
				}
				
			}
			finally
			{
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}

			mHandler.removeCallbacks(mDrawPattern);
			if (mVisible)
			{
				mHandler.postDelayed(mDrawPattern, 1000 / 25);
			}
		}
		void drawPattern(Canvas c)
		{	
			if(touched){
				currentFrame=0;
				paused=true;
				sit();
				x=(int) mTouchX-64;
				y=(int) mTouchY-64;
				x2=x;
				y2=y;
			}else if(paused){
				currentFrame=0;
				Pose[] walk = new Pose[1]; 
				walk[0]= new Pose(1,64,128,0,0,250);
				paused=false;
			}
			//TODO optimize
//			c.clipRect(0, 0, c.getWidth(), c.getHeight());
			c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

			if(background!=null){
				//TODO finish pos setup
				String pos =mPreferences.getString("pos", "center");
				int wid = background.getWidth();
				if(pos.equals("Center")||pos.equals("center")){
					c.drawBitmap(background,0-(wid-initX)/2, 0, null);
				}else if(pos.equals("Left Align")||pos.equals("left")){
					c.drawBitmap(background, 0, 0, null);
				}else if(pos.equals("Right Align")||pos.equals("right")){
					c.drawBitmap(background, 0-(wid-initX), 0, null);
				}
			}
//			c.clipRect(x, y, x+128, y+128);
			if(paused){
				velX = x-preX;
				velY = y-preY;  
			}else{
				if(fallingDisabled){}else{
					airControl();
				}
				//If loop is on
				if(onFloor()&&onLeftWall()){
					velX = 0;
					velY= 0;
					currentFrame=0;
					y2=y-6;
					y=y2;
					choice="up";
					climbWall();
					delayChoose = 150;
					delayTimer = action[currentFrame].getDuration();
				}else if(onFloor()&&onRightWall()){
					velX = 0;
					velY= 0;
					currentFrame=0;
					y2=y-6;
					y=y2;
					choice="up";
					climbWall();
					delayChoose = 150;
					delayTimer = action[currentFrame].getDuration();
				}else if(onCeiling()&&onLeftWall()){
					velX = 0;
					velY= 0;
					currentFrame=0;
					//50% chance of either continue climbing ceiling or climbing the wall
					if(Math.random()<.5){
						x2=x+30;
						x=x2;
						climbCeiling();
					}else{
						y2=y+5;
						climbWall();
					}
					choice = "right";
					left = false;
					delayChoose = 150;
					delayTimer = action[currentFrame].getDuration();				}
				else if(onCeiling()&&onRightWall()){
					velX = 0;
					velY= 0;
					currentFrame = 0;
					//50% chance of either continue climbing ceiling or climbing the wall
					if(Math.random()<.5){
						x2=x-30;
						x=x2;
						climbCeiling();
					}else{
						y2=y+5;
						climbWall();
					}
					choice = "left";
					left = true;
					delayChoose = 150;
					delayTimer = action[currentFrame].getDuration();
					
					//Either climb or fall
					//Right or left
					//Make sure to face right direction
				}else if(onLeftWall()){
					velX = 0;
					velY= 0;
					left=true;
					if(preInAir){
						climbWall();
						delayTimer = 0;
						delayChoose = 0;
						preInAir=false;
					}
					//Either climb or fall
					//Up or down
					if(delayChoose > 0){
						delayChoose--;
						if(choice.equals("up")){
							//climb up
							y2=y+action[currentFrame].getYVel();
							y=y2;
						}else if(choice.equals("down")){
							//climb down
							y2=y-action[currentFrame].getYVel();
							y=y2;
						}else if(choice.equals("fall")){
//							delayTimer = 0;
							//fall
							x2=x+1;
							x=x+1;	
							left = false;
						}else if(choice.equals("stay")){
							//stay in current position
							//do nothing else
							delayTimer=20;
						}
						
					}else{
						delayChoose = (int) Math.round(Math.random()*200);
						double rand =Math.random();
						if(rand<.6){
							//climb up
							choice = "up";
						}else if(rand<.7){
							//climb down
							choice = "down";
						}else if(rand<.91){
							//fall
							choice = "fall";
						}else if(rand<1){
							//stay in current position
							choice = "stay";
							delayChoose = delayChoose/2;
						}
					}
					//drop off to ground
					if(y==initY-1){
						x=x+2;
					}
				}else if(onRightWall()){
					velX = 0;
					velY= 0;
					left= false;
					if(preInAir){
						climbWall();
						delayTimer = 0;
						delayChoose = 0;
						preInAir=false;
					}
					//if nearing bottom
					
					//Either climb or fall
					//Up or down
					if(delayChoose > 0){
						delayChoose--;
						if(choice.equals("up")){
							//climb up
							y2=y+action[currentFrame].getYVel();
							y=y2;
						}else if(choice.equals("down")){
							//climb down
							y2=y-action[currentFrame].getYVel();
							y=y2;
						}else if(choice.equals("fall")){
							//fall
							x2=x-1;
							x=x-1;
							left=true;
						}else if(choice.equals("stay")){
							//stay in current position
							//do nothing else
							delayTimer=20;
						}
					}else{
						delayChoose = (int) Math.round(Math.random()*200);
						double rand =Math.random();
						if(rand<.6){
							//climb up
							choice = "up";
						}else if(rand<.9){
							//climb down
							choice = "down";
						}else if(rand<.91){
							//fall
							choice = "fall";
						}else if(rand<1){
							//stay in current position
							choice = "stay";
							delayChoose = delayChoose/2;
						}
					}
					//drop off to ground
					if(y==initY-1){
						x=x-2;
					}
				}else if(onCeiling()){
					velX = 0;
					velY= 0;
					if(preInAir){
						climbCeiling();
						delayTimer = 0;
						delayChoose = 0;
						preInAir=false;
						
					}
					
					//Either climb or fall
					//Up or down
					if(delayChoose > 0){
						delayChoose--;
						if(choice.equals("right")){
							//climb right	
							x2=x-action[currentFrame].getXVel();
							
						}else if(choice.equals("left")){
							//climb left
							x2=x+action[currentFrame].getXVel();
	
						}else if(choice.equals("fall")){
//							delayTimer = 0;
							//fall
							y2=y+1;
						}else if(choice.equals("stay")){
							//stay in current position
							//do nothing else
							delayTimer=20;
						}
					}else{
						delayChoose = (int) Math.round(Math.random()*200);
						double rand =Math.random();
						if(rand<.5){
							//climb up
							choice = "left";
							left = true;
						}else if(rand<.9){
							//climb down
							choice = "right";
							left = false;
						}else if(rand<.91){
							//fall
							choice = "fall";
						}else if(rand<1){
							//stay in current position
							choice = "stay";
							delayChoose = delayChoose/2;
						}
					}
				}else if(onFloor()){
					velX = 0;
					velY= 0;
					if(preInAir){
						currentFrame=0;
						bouncing();
						delayTimer = action[currentFrame].getDuration();
						delayTimer = 0;
						delayChoose = 4;
						preInAir=false;
						bounceTimer = 2;
						floorChoice="sit";
					}
					
//					if(bounceTimer > 0 ){
//						bounceTimer--;
//					}else{
//						walk();
//					}
//					
//					left=true;
//					x2=x+action[currentFrame].getXVel();
					
					//TODO
					/*
					Walk 
					| Left or Right
					*While walking add chance of tripping
					
					Run 
					| Left or Right
					*While Running add higher chance of tripping
					
					Dash 
					| Left or Right
					*While Dashing add even chance of tripping
					
					Sit
					|**Sitting options
					|lie down
					||crawl
					|||left or right

					 */
					if(delayChoose > 0){
						delayChoose--;
						if(floorChoice.equals("stay")){
							delayTimer=2;
							currentFrame = 0;
							if(Math.random()<.01){
								floorChoice="sit";
								delayChoose =0 ;
							}
						}else if(floorChoice.equals("walk")){
							if(left){
								x2=x+action[currentFrame].getXVel();
							}else{
								x2=x-action[currentFrame].getXVel();
							}
							//trip
							if(Math.random()<.005/4){
//								delayTimer=15;
								floorChoice="sit";
								delayChoose =0 ;
								tripping();
							}
						}else if(floorChoice.equals("run")){
							if(left){
								x2=x+action[currentFrame].getXVel();
							}else{
								x2=x-action[currentFrame].getXVel();
							}
							//trip
							if(Math.random()<.007/4){
//								delayTimer=15;
								floorChoice="sit";
								delayChoose =0 ;
								tripping();
							}
						}else if(floorChoice.equals("dash")){
							if(left){
								x2=x+action[currentFrame].getXVel();
							}else{
								x2=x-action[currentFrame].getXVel();
							}
							//trip
							if(Math.random()<.01/4){
								currentFrame = 0;
								delayTimer=0;
								floorChoice="sit";
								delayChoose =0 ; 
								tripping();
								
							}
						}else if(floorChoice.equals("sit")){
							//lay down
							if(Math.random()<.01/4){
								currentFrame = 0;
								delayTimer=0;
								floorChoice="lay";
								delayChoose =0 ; 
								sprawl();
							}
							//Stand back up
							if(Math.random()<.01/4){
								//System.out.println("Choose stay");
								currentFrame = 0;
								delayTimer=0;
								floorChoice="stay";
								delayChoose =0 ; 
								walk();
								delayChoose = (int) Math.round(Math.random()*100);
							}
						}else if(floorChoice.equals("lay")){
							//do nothing yet
						}else if(floorChoice.equals("creep")){
							if(left){
								x2=x+action[currentFrame].getXVel();
							}else{
								x2=x-action[currentFrame].getXVel();
							}
							//Stops moving and sprawls
							if(Math.random()<.01/4){
								currentFrame = 0;
								delayTimer=0;
								floorChoice="lay";
								delayChoose =0 ; 
								sprawl();
							}
						}
					}else{
						currentFrame = 0;
						delayChoose = (int) Math.round(Math.random()*600);
						//System.out.println("Test "+delayChoose);
						double rand = Math.random();
						if(floorChoice.equals("sit")){//System.out.println("Test2 "+delayChoose);
							if(rand < .2){
								sitAndLookUp();
							}else if(rand<.4){
								sitAndSpinHeadAction();
							}else if(rand<.6){
								sitWithLegsUp();
							}else if(rand<.8){
								sitWithLegsDown();
							}else if(rand<1){
								sitAndDangleLegs();
							}
						}else if(floorChoice.equals("lay")){
							//System.out.println("Test 3 "+delayChoose);
							if(rand < .5){
								sprawl();
							}else{
								floorChoice = "creep";
								creep();
								if(Math.random()<.5){
									left = true;
								}else{
									left = false;
								}
							}
						}else{
							//System.out.println("Test 4 "+delayChoose);
							if(rand < .4){
								floorChoice="walk";
								walk();
								if(Math.random()<.5){
									left = true;
								}else{
									left = false;
								}
							}else if(rand<.6){
								run();
								floorChoice="run";
								if(Math.random()<.5){
									left = true;
								}else{
									left = false;
								}
							}else if(rand<.7){
								dash();
								floorChoice="dash";
								if(Math.random()<.5){
									left = true;
								}else{
									left = false;
								}
							}else if(rand<1){
								walk();
								currentFrame=0;
								floorChoice="stay";
								if(Math.random()<.5){
									left = true;
								}else{
									left = false;
								}
							}
						}
					}
					
					
				}
				//TODO fix for changed bounds on ceiling
				if(action[currentFrame].getY()==128||onFloor()==false||onLeftWall()||onRightWall()||onCeiling()){
					//Prevent out of bounds movement
					if(x2<-64){
						x2=-64;
						//System.out.println("out");
					}else if(x2>initX-64){//System.out.println("out");
						x2=initX-64;
					}
					if(y2<-statBarHeight){
						y2=-statBarHeight;
					}else if(y2>initY){//System.out.println("out");
						y2=initY;
					}
					
					if(x<-64){//System.out.println("out");
						x=-64;
					}else if(x>initX-64){//System.out.println("out");
						x=initX-64;
					}
					if(y<-statBarHeight){//System.out.println("out");
						y=-statBarHeight;
					}else if(y>initY){//System.out.println("out");
						y=initY;
					}
					fallingDisabled = false;
				}else{
					y=initY+(128-action[currentFrame].getY());
					y2=y;
					fallingDisabled = true;
				}
				//System.out.println(floorChoice+" "+delayChoose+" "+delayTimer);
				//Delay frames
				if(delayTimer>0){
					delayTimer--;
					y2=y;
					x2=x;
				}else{
					x=x2;
					y=y2;
					//Change frames / loop through
					if(currentFrame==action.length-1){
						currentFrame=0;
					}else{
						currentFrame++;
					}
					delayTimer = action[currentFrame].getDuration()/2;
				}
			}
			
			
			//Set previous x and y for next velocity calc
			preX=x;
			preY=y;
			//left orientation
			if(imgFrames[action[currentFrame].getLocation()-1]==null){
				System.out.println("Null Images: "+mPreferences.getString("imgLoc", "Oliver"));
			}else{
				if(left){
					c.drawBitmap(imgFrames[action[currentFrame].getLocation()-1], x, y, null);
				}//flipped image to the right
				else{
					c.drawBitmap(flip(imgFrames[action[currentFrame].getLocation()-1]), x, y, null);
				}
			}System.out.println();
		}
		
		//Flips the image
		Bitmap flip(Bitmap imgFrames2)
		{
		    Matrix m = new Matrix();
		    m.preScale(-1, 1);
		    Bitmap src = imgFrames2;
		    Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
		    dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
		    return dst;
		}
		//Sets the current action:
		public void walk(){//System.out.println("Ran: walk");
			Pose[] walk = new Pose[4];
			walk[0]= new Pose(1,64,128,-2,0,4);
			walk[1]= new Pose(2,64,128,-2,0,4);
			walk[2]= new Pose(1,64,128,-2,0,4);
			walk[3]= new Pose(3,64,128,-2,0,4);
			action = walk;
		}
		public void run(){//System.out.println("Ran: run");
			Pose[] run = new Pose[4];
			run[0]= new Pose(1,64,128,-4,0,2);
			run[1]= new Pose(2,64,128,-4,0,2);
			run[2]= new Pose(1,64,128,-4,0,2);
			run[3]= new Pose(3,64,128,-4,0,2);
			action = run;
		}
		public void dash(){//System.out.println("Ran: dash");
			Pose[] dash = new Pose[4];
			dash[0]= new Pose(1,64,128,-8,0,2);
			dash[1]= new Pose(2,64,128,-8,0,2);
			dash[2]= new Pose(1,64,128,-8,0,2);
			dash[3]= new Pose(3,64,128,-8,0,2);
			action = dash;
		}
		public void sit(){//System.out.println("Ran: sit");
			Pose[] sit = new Pose[1];
			sit[0]= new Pose(11,64,128,0,0,250);
			action = sit;
		}
		public void sitAndLookUp(){//System.out.println("Ran: sitAndLookUp");
			Pose[] sitAndLookUp = new Pose[1];
			sitAndLookUp[0]= new Pose(26,64,128,0,0,250);
			action = sitAndLookUp;
		}
		public void sitAndSpinHeadAction(){//System.out.println("Ran: sitAndSpinHeadAction");
			Pose[] sitAndSpinHeadAction = new Pose[8];
			sitAndSpinHeadAction[0]= new Pose(26,64,128,0,0,5);
			sitAndSpinHeadAction[1]= new Pose(15,64,128,0,0,5);
			sitAndSpinHeadAction[2]= new Pose(27,64,128,0,0,5);
			sitAndSpinHeadAction[3]= new Pose(16,64,128,0,0,5);
			sitAndSpinHeadAction[4]= new Pose(28,64,128,0,0,5);
			sitAndSpinHeadAction[5]= new Pose(17,64,128,0,0,5);
			sitAndSpinHeadAction[6]= new Pose(29,64,128,0,0,5);
			sitAndSpinHeadAction[7]= new Pose(11,64,128,0,0,5);
			action = sitAndSpinHeadAction;
		}
		public void sitWithLegsUp(){//System.out.println("Ran: sitWithLegsUp");
			Pose[] sitWithLegsUp = new Pose[1];
			sitWithLegsUp[0]= new Pose(30,64,112,0,0,250);			
			action = sitWithLegsUp;
		}
		public void sitWithLegsDown(){//System.out.println("Ran: sitWithLegsDown");
			Pose[] sitWithLegsDown = new Pose[1];
			sitWithLegsDown[0]= new Pose(31,64,112,0,0,250);
			action = sitWithLegsDown;
		}
		public void sitAndDangleLegs(){//System.out.println("Ran: sitAndDangleLegs");
			Pose[] sitAndDangleLegs = new Pose[4];
			sitAndDangleLegs[0]= new Pose(31,64,112,0,0,5);
			sitAndDangleLegs[1]= new Pose(32,64,112,0,0,15);
			sitAndDangleLegs[2]= new Pose(31,64,112,0,0,5);
			sitAndDangleLegs[3]= new Pose(33,64,112,0,0,15);
			action = sitAndDangleLegs;
		}
		public void sprawl(){//System.out.println("Ran: sprawl");
			Pose[] sprawl = new Pose[1];
			sprawl[0]= new Pose(21,64,128,0,0,250);
			action = sprawl;
		}
		public void creep(){//System.out.println("Ran: creep");
			Pose[] creep = new Pose[5];
			creep[0]= new Pose(20,64,128,0,0,10);
			creep[1]= new Pose(20,64,128,-2,0,4);
			creep[2]= new Pose(21,64,128,-2,0,4);
			creep[3]= new Pose(21,64,128,-1,0,4);
			creep[4]= new Pose(21,64,128,0,0,10);
			action = creep;
		}
		/*Ceiling */
		public void grabCeiling(){
			Pose[] grabCeiling = new Pose[1];
			grabCeiling[0]= new Pose(23,64,128,0,0,250);
			action = grabCeiling;
		}
		public void climbCeiling(){
			Pose[] climbCeiling = new Pose[8];
			climbCeiling[0]= new Pose(25,64,48,0,0,16);
			climbCeiling[1]= new Pose(25,64,48,-1,0,4);
			climbCeiling[2]= new Pose(23,64,48,-1,0,4);
			climbCeiling[3]= new Pose(24,64,48,-1,0,4);
			climbCeiling[4]= new Pose(24,64,48,0,0,16);
			climbCeiling[5]= new Pose(24,64,48,-2,0,4);
			climbCeiling[6]= new Pose(23,64,48,-2,0,4);
			climbCeiling[7]= new Pose(25,64,48,-2,0,4);
			action = climbCeiling;
		}
		/* Wall */
		public void grabWall(){
			Pose[] grabWall = new Pose[1];
			grabWall[0]= new Pose(13,64,128,0,0,250);
			action = grabWall;
		}
		public void climbWall(){
			Pose[] climbWall = new Pose[8];
			climbWall[0]= new Pose(14,64,48,0,0,16);
			climbWall[1]= new Pose(14,64,48,0,-1,4);
			climbWall[2]= new Pose(12,64,48,0,-1,4);
			climbWall[3]= new Pose(13,64,48,0,-1,4);
			climbWall[4]= new Pose(13,64,48,0,0,16);
			climbWall[5]= new Pose(13,64,48,0,-2,4);
			climbWall[6]= new Pose(12,64,48,0,-2,4);
			climbWall[7]= new Pose(14,64,48,0,-2,4);
			action = climbWall;
		}
		/* Falling */
		public void falling(){//System.out.println("Ran: falling");
			Pose[] falling = new Pose[1];
			falling[0]= new Pose(4,64,128,0,0,250);
			action = falling;
		}
		/*Once hits floor*/
		public void bouncing(){//System.out.println("Ran: bouncing");
			Pose[] bouncing = new Pose[2];
			bouncing[0]= new Pose(18,64,128,0,0,4);
			bouncing[1]= new Pose(19,64,128,0,0,4);
			action = bouncing;
		}
		public void tripping(){
			//System.out.println("Ran: tripping");
			Pose[] tripping = new Pose[5];
			tripping[0]= new Pose(19,64,128,-8,0,8);
			tripping[1]= new Pose(18,64,128,-4,0,4);
			tripping[2]= new Pose(20,64,128,2,0,4);
			tripping[3]= new Pose(20,64,128,0,0,10);
			tripping[4]= new Pose(19,64,128,-4,0,4);
			action = tripping;
		}
		//Air control
		public void airControl(){
			if(onAir()){
				preInAir=true;
				//set the facing orientation
				if(velX>0){
					left=true;
				}else{
					left=false;
				}
				
				currentFrame = 0;
				if(velX<5&&velX>=0){
					if(touched==false){
						falling();
					}
				}
				
					if(velX>10&&velX<=5){
						action = new Pose[1];
						action[0]= new Pose(6,64,128,0,0,250);
					}else
					if(velX>10&&velX<=5){
						action = new Pose[1];
						action[0]= new Pose(8,64,128,0,0,250);
					}else
					if(velX<=10){
						action = new Pose[1];
						action[0]= new Pose(10,64,128,0,0,250);
					}
				
					if(velX<10&&velX>=5){
						action = new Pose[1];
						action[0]= new Pose(5,64,128,0,0,250);
					}else
					if(velX<20&&velX>=10){
						action = new Pose[1];
						action[0]= new Pose(7,64,128,0,0,250);
					}else
					if(velX>=20){
						action = new Pose[1];
						action[0]= new Pose(9,64,128,0,0,250);
					}
				
				
				x2=x+velX;
				y2=y+velY;
				x=x2;
				y=y2;
				if(velY<35){
					velY = velY+5;
				}

			}
		}
		
		//Current Location methods
		public boolean onFloor(){
			return (y == initY)||(y==initY+(128-action[currentFrame].getY()));
		}
		public boolean onCeiling(){
			return y == -statBarHeight;
		}
		public boolean onLeftWall(){
			return x == -64;
		}
		public boolean onRightWall(){
			return x == initX-64;
		}
		public boolean onAir(){
			return (onFloor()||onCeiling()||onLeftWall()||onRightWall())==false;
		}
		//Preload all images for use in animation
		private void loadImages() {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			// Calculate inSampleSize
//			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			options.inPreferredConfig = Config.RGB_565;
			options.inDither = true;
			String frames[] = null;
			try {
				frames = man.list("Oliver");
			} catch (IOException e) {
				System.out.println("Null List");
			}
			imgFrames = new Bitmap[frames.length];  // allocate the array
			for (int i = 0; i < frames.length; ++i) {
				try {
					if(mPreferences.getString("imgLoc", "Oliver").equals("Oliver")){
						imgFrames[i] = BitmapFactory.decodeStream(man.open("Oliver/shime"+(i+1)+".png"),null, options);
					}else{
						imgFrames[i] = BitmapFactory.decodeFile(mPreferences.getString("imgLoc", "Oliver")+"/shime"+(i+1)+".png",options);
						if(imgFrames[i]==null){
							throw new IOException();
						}
					}
				} catch (IOException e) {
					System.out.println("File or Directory Not Found: "+mPreferences.getString("imgLoc", "Oliver"));
					mEditor.putString("imgLoc", "Oliver");
					mEditor.commit();
					loadImages();
				}
			}
//			imgWidth = imgFrames[0].getWidth();
//			imgHeight = imgFrames[0].getHeight();
		}
	}
}