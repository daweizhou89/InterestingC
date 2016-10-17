package com.github.daweizhou89.rollview;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.github.daweizhou89.rollview.math.Vector2;
import com.github.daweizhou89.rollview.utils.FpsManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RollView extends View implements IRollView {

	private static final boolean DEBUG_FPS = true;
	private static final boolean DEBUG_LOG = true;
	private static final String TAG = "dawei";
    public static final String TEXT_DEFAULT_NO_DATA_TIPS = "暂无数据";

    private Camera mCamera;
    private Matrix mMatrix;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG
            | Paint.FILTER_BITMAP_FLAG);
    private DisplayMetrics mDisplayMetrics;
    private float mBaseX;
    private float mBaseY;
    private int mBallRadius;
    private float mScale;

    private RollViewAdapter mAdapter;
    protected RollController mRollController;
    private FpsManager mFpsManager;

    List<IRollCellClickedListener> mRollCellClickedListeners = new ArrayList<IRollCellClickedListener>();
    IRollViewTouchListener mRollViewTouchListener;
    
    public void setRollViewTouchListener(IRollViewTouchListener rollViewTouchListener) {
    	mRollViewTouchListener = rollViewTouchListener;
    }
    
    public void removeRollViewTouchListener() {
    	mRollViewTouchListener = null;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;
        final int height = bottom - top;
        mBaseX = width / 2.f;
        mBaseY = height / 2.f;
        mScale = width < height ? ((float)width / mDisplayMetrics.widthPixels) : ((float)height / mDisplayMetrics.widthPixels);
        mBallRadius =  mDisplayMetrics.widthPixels / 2;
        mBallRadius = mBallRadius * 8 / 9;
        mRollController.setBallRadius(mBallRadius);
    }

    public RollView(Context context) {
        this(context, null);
    }
    
    public RollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public RollView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        mCamera = new Camera();
        mMatrix = new Matrix();
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        mPaint.setTextSize(mDisplayMetrics.scaledDensity * 10);
        mFpsManager = new FpsManager(30.0F, 8.0F, 10);
        mRollController = new RollController(context, this);
    }
    
    @Override
    protected void onAttachedToWindow() {
    	if (DEBUG_LOG) Log.v(TAG, "onAttachedToWindow!");
        super.onAttachedToWindow();
        setRollViewTouchListener(getRollViewTouchListenerFromParents(this));
        resume();
    }
    
    private IRollViewTouchListener getRollViewTouchListenerFromParents(View view) {
    	IRollViewTouchListener ret = null;
    	LinkedList<View> queue = new LinkedList<View>();
    	queue.offer(view);
    	while(!queue.isEmpty()) {
    		View polled = queue.poll();
    		if(polled == null) break;
    		ViewParent parent = polled.getParent();
        	if (parent != null) {
        		if(parent instanceof IRollViewTouchListener) {
        			ret = (IRollViewTouchListener) parent;
        			if (DEBUG_LOG) Log.v(TAG, "IRollViewTouchListener is found!");
        			break;
        		} else if(parent instanceof View) {
        			if (DEBUG_LOG) Log.v(TAG, "IRollViewTouchListener is not found!");
        			queue.offer((View)parent);
        		}
        	}
    	}
		return ret;
	}

	@Override
    protected void onDetachedFromWindow() {
		if (DEBUG_LOG) Log.v(TAG, "onDetachedFromWindow!");
        super.onDetachedFromWindow();
        removeRollViewTouchListener();
        pause();
    }
    
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility == View.VISIBLE) {
            resume();
        } else {
            pause();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mFpsManager.preProcess();
        
        super.onDraw(canvas);
        
        if (DEBUG_FPS) canvas.drawText("[fps : " + mFpsManager.getFps() + " ]", 0, 50, mPaint);
        canvas.save();
        canvas.translate(mBaseX, mBaseY);
        canvas.scale(mScale, mScale);
        mRollController.draw(canvas, mCamera, mMatrix);
        canvas.restore();
        
        mFpsManager.postProcess();
        
        if (mRollController.isToRoll() && !mIsPause) {
            // roll
            mRollController.roll();
            // invalidate
            postInvalidateDelayed(mFpsManager.getDelayTime());
        }
    }

    private Vector2 mStartPosition = new Vector2();
    private Vector2 mDirectedPosition = new Vector2();
    private static final int DRAG_STATE_REST = 0;
    private static final int DRAG_STATE_DRAG = 1;
    private static final int DRAG_STATE_NO_DRAG = 2;
    private int mDragState = DRAG_STATE_REST;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isInBall(x - mBaseX, y - mBaseY)) ret = false;
                mDragState = DRAG_STATE_REST;
                mStartPosition.x = x;
                mStartPosition.y = -y;
                break;
            case MotionEvent.ACTION_MOVE:
                ret = filteDrag(x, y);
            	break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mDragState == DRAG_STATE_NO_DRAG) {
                    mIsLongClick = false;
                    mIsToClick = false;
                }
                break;
            default:
                break;
        }
        if (ret)
            ret = super.dispatchTouchEvent(event);
        return ret;
    }
    
    private boolean filteDrag(float x, float y) {
        boolean ret = true;
        if(mDragState == DRAG_STATE_REST && mStartPosition.dst(x, -y) > 3) { 
            if (isHorizonDrag(x, -y)) {
                mDragState = DRAG_STATE_NO_DRAG;
                if (mRollViewTouchListener != null) mRollViewTouchListener.onRollTouchEnd();
                ret = false;
            } else {
                mDragState = DRAG_STATE_DRAG;
            }
        } else if(mDragState == DRAG_STATE_NO_DRAG) {
            ret = false;
        }
        return ret;
    }

    private boolean isHorizonDrag(float x, float y) {
        boolean ret = false;
        float tanAlpha = Math.abs(y - mStartPosition.y) / Math.abs(x - mStartPosition.x);
        double alpha = Math.abs(Math.atan(tanAlpha));
        if(alpha < Math.PI / 6) ret = true;
        return ret;
    }

    private boolean mIsToClick;
    private boolean mIsLongClick;
    private static final int DEFAULT_LONG_CLICK_DELAY = 500;
    private long mClickBeginTime;
    private boolean mIsPause;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean ret = true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	if (mRollViewTouchListener != null) mRollViewTouchListener.onRollTouchBegin();
            	mClickBeginTime = SystemClock.uptimeMillis();
                mIsToClick = true;
                mIsLongClick = false;
                pause();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mStartPosition.dst(x, -y) > 3) {
                	checkForLongClick();
                    mDirectedPosition.x = x;
                    mDirectedPosition.y = -y;
                    mRollController.setDirection(mStartPosition, mDirectedPosition);
                    if (!mRollController.isUniformAndCircular())
                        mRollController.setAngularAcceleration(0.0f);
                    if (mIsToClick) {
                        resume();
                        mFpsManager.reset();
                        refresh();
                    }
                    mIsToClick = false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            	checkForLongClick();
                if (!mIsToClick) {
                    mDirectedPosition.x = x;
                    mDirectedPosition.y = -y;
                    mRollController.setDirection(mStartPosition, mDirectedPosition);
                    if (!mRollController.isUniformAndCircular())
                        mRollController.restoreAngularAcceleration();
                } else if(!mIsLongClick) {
                    if (!checkRollCellClicked(x, y)) {
                        resume();
                    }
                }
                mIsLongClick = false;
                mIsToClick = false;
                if (mRollViewTouchListener != null) mRollViewTouchListener.onRollTouchEnd();
                break;
            default:
                break;
        }

        if (!ret)
            ret = super.onTouchEvent(event);

        return ret;
    }

    private void checkForLongClick() {
		if (mIsToClick && !mIsLongClick) {
			if(SystemClock.uptimeMillis() - mClickBeginTime > DEFAULT_LONG_CLICK_DELAY) {
				mIsLongClick = true;
			}
		}
	}

    private boolean checkRollCellClicked(float x, float y) {
        boolean ret = false;
        float xInBall = x - mBaseX;
        float yInBall = y - mBaseY;
        if (isInBall(xInBall, yInBall)) {
            RollCell rollCell = mRollController.checkClick(xInBall, yInBall);
            if (rollCell != null) {
                ret = true;
                perfromClick(x, y, rollCell);
            }
        }
        return ret;
    }

    private synchronized void perfromClick(float x, float y, RollCell rollCell) {
        for (IRollCellClickedListener listener : mRollCellClickedListeners) {
            listener.onRollCellClicked(x, y, rollCell);
        }
    }

    private boolean isInBall(float x, float y) {
        return Math.abs(x) < mBallRadius && Math.abs(y) < mBallRadius;
    }

    public void setNoDataTips(String tips) {
        mRollController.setNoDataTips(tips);
    }

    public void setRollViewAdapter(RollViewAdapter adapter) {
        mAdapter = adapter;
        if (mAdapter != null) {
            mPaint.setColor(mAdapter.getTextColor());
        }
        if (mRollController != null) {
            mRollController.setRollViewAdapter(adapter);
        }
    }

    public synchronized void addRollCellClickedListener(
            IRollCellClickedListener rollCellClickedListeners) {
        if (rollCellClickedListeners != null
                && !mRollCellClickedListeners.contains(rollCellClickedListeners)) {
            mRollCellClickedListeners.add(rollCellClickedListeners);
        }
    }

    public synchronized void removeRollCellClickedListener(
            IRollCellClickedListener rollCellClickedListeners) {
        if (rollCellClickedListeners != null) {
            mRollCellClickedListeners.remove(rollCellClickedListeners);
        }
    }

    public synchronized void removeRollCellClickedListeners() {
        if (!mRollCellClickedListeners.isEmpty()) {
            mRollCellClickedListeners.clear();
        }
    }

    public void pause() {
        mIsPause = true;
    }

    public void resume() {
        mFpsManager.reset();
        mIsPause = false;
        refresh();
    }

    @Override
    public void refresh() {
        postInvalidate();
    }

    @Override
    public void refreshDelayed(long delay) {
        postInvalidateDelayed(delay);
    }
}
