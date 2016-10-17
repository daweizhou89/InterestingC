
package com.github.daweizhou89.rollview.utils;

public class FpsManager {

    
    private int mCount = 0;
    private int mResetCount;
    private float mActualFpS;
    public float mTargetFps;
    private float mDelayTime;
    private float mMiniumDelayTime;
    private float mOptimizedDelayTime = 0L;
    private long mStartTimeFps;
    private long mStartTimeFrame;
    private long mStopTimeFrame;
    private long mDrawTime;

    public FpsManager(float targetFps, float miniumDelayTime, int resetCount) {
        this.mTargetFps = targetFps;
        this.mMiniumDelayTime = miniumDelayTime;
        mResetCount = resetCount;
    }

    public long getDelayTime() {
        return (long) mOptimizedDelayTime;
    }

    public long getDrawTimeMs() {
        return this.mDrawTime;
    }

    public float getFps() {
        return mActualFpS;
    }

    public void postProcess() {
        mStopTimeFrame = System.currentTimeMillis();
        mDrawTime = mStopTimeFrame - mStartTimeFrame;
        if (mCount == mResetCount) {
            mActualFpS = (mCount / ((float)(System.currentTimeMillis() - mStartTimeFps) / 1000.0F));
            mCount = -1;
            float deltaFps = mTargetFps - mActualFpS;
            mDelayTime = Math.max(0.0F, mDelayTime - 0.5F * deltaFps);
        }
        mOptimizedDelayTime = Math.max(mMiniumDelayTime, mDelayTime- (float) mDrawTime);
        mCount++;
    }

    public void preProcess() {
        mStartTimeFrame = System.currentTimeMillis();
        if (mCount == 0) {
            mStartTimeFps = System.currentTimeMillis();
        }
    }

    public void reset() {
        mCount = 0;
        mActualFpS = 0.f;
        mDelayTime = 0.f;
        mOptimizedDelayTime = 0L;
        mStartTimeFps = 0L;
        mStartTimeFrame = 0L;
        mStopTimeFrame = 0L;
        mDrawTime = 0L;
    }
}