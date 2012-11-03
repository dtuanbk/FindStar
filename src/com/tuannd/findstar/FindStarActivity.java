package com.tuannd.findstar;

import java.util.Random;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.debug.Debug;

import android.util.DisplayMetrics;
import android.widget.Toast;

import com.tuannd.findstar.config.Config;
import com.tuannd.findstar.log.IErrorLog;
import com.tuannd.findstar.log.Logging;


public class FindStarActivity extends BaseGameActivity implements IScrollDetectorListener,IOnSceneTouchListener,IPinchZoomDetectorListener{
	
	private final String TAG=FindStarActivity.this.getClass().getSimpleName(); 
	public IErrorLog log = null;
	
	//Khai báo width và height
	public int CAMERA_WIDTH = 800;
	public int CAMERA_HEIGHT = 480;
	
	//Khai báo camera
	public ZoomCamera mCamera;
	
	
	
	//Khai báo TileMap
	private TMXTiledMap mTMXTiledMap;
	public TMXLayer tmxLayer;
	
	
	
	//Path Resources
	private String path_Resources="images/";
	
	
	//Khai báo star
	private BitmapTextureAtlas mStar_BitmapTextureAtlas;
	private ITextureRegion mStar_ITextureRegion;
	private Sprite mStar_Sprite;
	
	
	//Khai báo arrow
	private BitmapTextureAtlas mArrow_BitmapTextureAtlas;
	private ITiledTextureRegion mArrow_ITiledTextureRegion;
	private AnimatedSprite mArrow_AnimatedSprite;
	
	
	
	//Khai báo Scence
	private Scene mScene;
	
	
	//Khai báo scroll
	public ScrollDetector mScrollDetector;
	public PinchZoomDetector mPinchZoomDetector;
	public float mPinchZoomStartedCameraZoomFactor;
	private final float zoomDepth = 2f; // Smaller this is, the less we zoom in?
	private final float minZoom = 1f;
	
	
	//Clickable
	private boolean mClicked = false;
	
	
	
	private static final int LEFT=0;
	private static final int BOTTOM=1;
	private static final int RIGHT=2;
	private static final int UP=3;
	private static final int FINISHED=4;
	
	
	//Mang chua gia tri cac phan tu trong mang da duoc chon hay chua?
	
	private int[][] arr;
	
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub
		
		/*
		 * Khởi tạo Logging
		 */
		this.log = new Logging();
		this.log.setTag(this.TAG);
		this.log.enable(true);
		
		this.log.i(0, "onCreateEngineOptions");
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		CAMERA_WIDTH = displayMetrics.widthPixels;
		CAMERA_HEIGHT = displayMetrics.heightPixels;
		this.mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions eOps = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		return eOps;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		// TODO Auto-generated method stub
		this.log.i(0, "onCreateResources");
		
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		
		//Khởi tạo assets path resources
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(path_Resources);
		
		loadOnCreateResources();
		pOnCreateResourcesCallback.onCreateResourcesFinished();
		
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub
		this.log.i(0, "onCreateScene");
		
		
		//FPS
		final FPSLogger fpsLogger = new FPSLogger();
		this.getEngine().registerUpdateHandler(fpsLogger);
		
		
		mScene = new Scene();
		mScene.setBackground(new Background(1f, 1f, 1f,0.5f));
		tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxLayer);
		
		
		//
		this.mCamera.setBounds(-100, -100, tmxLayer.getHeight()+100, tmxLayer.getWidth()+100);
        this.mCamera.setBoundsEnabled(true);
		
        
        //Xét giá trị cho dirX và dirY
        Config.dirX=new Random().nextInt(tmxLayer.getTileColumns());
        Config.dirY=new Random().nextInt(tmxLayer.getTileRows());
        
        arr=new int[tmxLayer.getTileColumns()][tmxLayer.getTileRows()]; 
        System.out.println("dirX="+Config.dirX+"   dirY="+Config.dirY);
        
		mScene.setOnSceneTouchListener(FindStarActivity.this);
		mScene.setTouchAreaBindingOnActionMoveEnabled(true);
		
		
		
		
		
		pOnCreateSceneCallback.onCreateSceneFinished(this.mScene);
		
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		// TODO Auto-generated method stub
		this.log.i(0, "onPopulateScene");
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	/*-------------------------------------------------------------------------------
	 * -----------------bất đầu Load resources---------------------------------------
	 * ------------------------------------------------------------------------------
	 */
	
	
	public void loadOnCreateResources(){
		loadStar_Resources();
		loadMap_Resources();
		loadArrow_Resources();
	}
	
	public void loadStar_Resources(){
		this.mStar_BitmapTextureAtlas=new BitmapTextureAtlas(this.getTextureManager(), 44, 44,TextureOptions.DEFAULT);
		this.mStar_ITextureRegion=BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mStar_BitmapTextureAtlas, this, "star_1.png", 0, 0);
		
		this.mStar_BitmapTextureAtlas.load();
	}
	
	
	public void loadArrow_Resources(){
		this.mArrow_BitmapTextureAtlas=new BitmapTextureAtlas(this.getTextureManager(), 88, 88,TextureOptions.BILINEAR);
		this.mArrow_ITiledTextureRegion=BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mArrow_BitmapTextureAtlas, this, "arrow.png", 0, 0,2,2);
		
		this.mArrow_BitmapTextureAtlas.load();
	}
	
	public void loadMap_Resources(){
		try {
			final TMXLoader tmxLoader = new TMXLoader(this.getAssets(), this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getVertexBufferObjectManager(), new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
					/* We are going to count the tiles that have the property "cactus=true" set. */
					
				}
			});
			this.mTMXTiledMap = tmxLoader.loadFromAsset("tmx/level_1.tmx");
		} catch (final TMXLoadException e) {
			Debug.e(e);
		}
	}
	/*-------------------------------------------------------------------------------
	 * -----------------Kết thúc Load resources--------------------------------------
	 * ------------------------------------------------------------------------------
	 */
	
	
	
	
	public void loadOnCreateScene(){
		
	}
	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener#onScrollStarted(org.andengine.input.touch.detector.ScrollDetector, int, float, float)
	 */

	@Override
	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		this.log.i(1, "onScrollStarted");
	}

	@Override
	public void onScroll(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		this.log.i(1, "onScroll");
		final float zoomFactor = mCamera.getZoomFactor();
		float xLocation = -pDistanceX / zoomFactor;
		float yLocation = -pDistanceY / zoomFactor;
		mCamera.offsetCenter(xLocation, yLocation);
		this.mClicked = false;
	}

	@Override
	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		this.log.i(1, "onScrollFinished");
	}
	/*
	 * (non-Javadoc)
	 * @see org.andengine.entity.scene.IOnSceneTouchListener#onSceneTouchEvent(org.andengine.entity.scene.Scene, org.andengine.input.touch.TouchEvent)
	 */

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		if (this.mPinchZoomDetector != null) {
			this.log.i(0, "PinchZoomDetector");
			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
			if (this.mPinchZoomDetector.isZooming()) {
				this.mScrollDetector.setEnabled(false);
			} else {
				if (pSceneTouchEvent.isActionDown()) {
					this.mScrollDetector.setEnabled(true);
				}
				this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
			}
		} else {
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		if (pSceneTouchEvent.isActionUp()) {
			if (this.mClicked) {

				this.handleActionDown(pScene, pSceneTouchEvent);

			}
			this.mClicked = true;
		}
		return true;
	}

	@Override
	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		this.log.i(1, "onPinchZoomStarted");
		this.mPinchZoomStartedCameraZoomFactor = this.mCamera.getZoomFactor();
		this.mClicked = false;
	}

	@Override
	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		// TODO Auto-generated method stub
		this.log.i(1, "onPinchZoom");
		this.mCamera.setZoomFactor(Math.min(
				Math.max(this.minZoom, this.mPinchZoomStartedCameraZoomFactor
						* pZoomFactor), this.zoomDepth));
		this.mClicked = false;
	}

	@Override
	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		// TODO Auto-generated method stub
		this.log.i(1, "onPinchZoomFinished");
		this.mClicked = false;
	}
	
	private void handleActionDown(final Scene pScene,
			TouchEvent pSceneTouchEvent) {
		this.log.i(4, String.format("Touch X: %f Y: %f",
				pSceneTouchEvent.getX(), pSceneTouchEvent.getY()));
		this.touchMap(pScene, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());

	}
	
	public void touchMap(final Scene pScene, final float pX, final float pY) {
//		final float[] pToTiles = this.getEngine().getScene()
//				.convertLocalToSceneCoordinates(pX, pY);
		TMXTile tmxSelected = this.tmxLayer.getTMXTileAt(pX,pY);
		System.out.println("Get XY Col:" + tmxSelected.getTileColumn() + "Row:"+ tmxSelected.getTileRow());
//		addStar(pScene, tmxSelected.getTileX(), tmxSelected.getTileY());
//		addArrow(pScene, tmxSelected.getTileX(), tmxSelected.getTileY(), LEFT);
		int result=checkFinished(tmxSelected.getTileColumn(),tmxSelected.getTileRow());
		if(arr[tmxSelected.getTileColumn()][tmxSelected.getTileRow()]==0){
			arr[tmxSelected.getTileColumn()][tmxSelected.getTileRow()]=result;
			addSprite(result, pScene, tmxSelected.getTileX(), tmxSelected.getTileY());
		}
		
	}
	
	
	
	//AddSprite
	public void addStar(Scene pScene,float x,float y){
		this.mStar_Sprite=new Sprite(x, y, this.mStar_ITextureRegion,this.getVertexBufferObjectManager());
		pScene.attachChild(this.mStar_Sprite);
	}
	
	public void addArrow(Scene pScene,float x,float y,int arrow){
		this.mArrow_AnimatedSprite=new AnimatedSprite(x, y, this.mArrow_ITiledTextureRegion, this.getVertexBufferObjectManager());
		this.mArrow_AnimatedSprite.setCurrentTileIndex(arrow);
		pScene.attachChild(this.mArrow_AnimatedSprite);
	}
   
	
	public int checkFinished(int x,int y){
		int result=-1;
		if(x==Config.dirX && y==Config.dirY){
			System.out.println("Finished");
			result=FINISHED;
		}
		if(x<Config.dirX){
			if(y<Config.dirY){
				if((new Random().nextInt(1))==0){
					result=RIGHT;
				}else result=BOTTOM;
			}else if(y==Config.dirY){
				result=RIGHT;
				
			}else if(y>Config.dirY){
				if((new Random().nextInt(1))==0){
					result=LEFT;
				}else result=BOTTOM;
			}
		}else if(x==Config.dirX){
			if(y<Config.dirY){
				result=BOTTOM;
			}else if(y>Config.dirY){
				result=UP;
			}
		}else if(x>Config.dirX){
			if(y<Config.dirY){
				if((new Random().nextInt(1))==0){
					result=LEFT;
				}else result=BOTTOM;
			}else if(y==Config.dirY){
				result=LEFT;
			}else if(y>Config.dirY){
				if((new Random().nextInt(1))==0){
					result=LEFT;
				}else{
					result=UP;
				} 
					
			}
		}
		return result;
	}
	
	public void addSprite(int result,Scene pScene,float x,float y){
		if(result==FINISHED){
			addStar(pScene, x, y);
			FindStarActivity.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(FindStarActivity.this, "Finished", Toast.LENGTH_SHORT).show();
				}
			});
		}else if(result==LEFT){
			addArrow(pScene, x, y, LEFT);
		}else if(result==RIGHT){
			addArrow(pScene, x, y, RIGHT);
		}else if(result==UP){
			addArrow(pScene, x, y, UP);
		}else if(result==BOTTOM){
			addArrow(pScene, x, y, BOTTOM);
		}
	}
}
