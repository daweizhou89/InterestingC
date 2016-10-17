package com.github.daweizhou89.rollview;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

import com.github.daweizhou89.rollview.math.Vector2;


public abstract class RollViewAdapter {

    private static final float DEFAULT_ANGULAR_VELOCITY_FACTOR = 300.f;
    public static final float DEFAULT_ANGULAR_ACCELERATION = -0.0001F;
    public static final int DEFAULT_SLICES = 9;
    public static final double DEFAULT_ANGULAR_VELOCITY = Math.PI / DEFAULT_ANGULAR_VELOCITY_FACTOR;
    public static final double DEFAULT_MAX_ANGULAR_VELOCITY = 2 * DEFAULT_ANGULAR_VELOCITY;
    public static final int DEFAULT_TEXT_SIZE = 12;
    public static final int DEFAULT_MIN_SLICES = 9;
    public static final int DEFAULT_MAX_SLICES = 9;

    private List<RollDataChangedListener> mChangedListeners = new ArrayList<RollDataChangedListener>();

    public int getSlices() {
        int slices = 0;
        int count = getCount();
        if (count > 0) {
            slices = (int) Math.sqrt(count / 2.f);
            if (slices < DEFAULT_MIN_SLICES) {
                slices = DEFAULT_MIN_SLICES;
            } else if (slices > DEFAULT_MAX_SLICES) {
                slices = DEFAULT_MAX_SLICES;
            }
        }
        return slices;
    }

    public int getTextSize() {
        return DEFAULT_TEXT_SIZE;
    }

    public int getTextColor() {
        return Color.BLACK;
    }

    /**
     * 是否匀速循环运动
     * 
     * @return
     */
    public boolean isUniformAndCircular() {
        return false;
    }

    /**
     * 获取角速度，仅当{@link RollView.isUniformAndCircular()}范围true有效
     * 
     * @return
     */
    public double getAngularVelocity() {
        return DEFAULT_ANGULAR_VELOCITY;
    }

    /**
     * 获取最大角速度，仅当{@link RollView.isUniformAndCircular()}范围false有效
     * 
     * @return
     */
    public double getMaxAngularVelocity() {
        return DEFAULT_MAX_ANGULAR_VELOCITY;
    }

    /**
     * 获取角加速度，仅当{@link RollView.isUniformAndCircular()}范围false有效
     * 
     * @return
     */
    public float getAngularAcceleration() {
        return DEFAULT_ANGULAR_ACCELERATION;
    }

    public int getRadius() {
        return -1;
    }

    /**
     * 获取角加速度，仅当{@link RollView.isUniformAndCircular()}范围true有效
     * 
     * @return
     */
    public float[] getDirection() {
        return new float[] {
                1.0f, -1.0f
        };
    }

    protected final Vector2 getAxis() {
        Vector2 ret = null;
        float[] axis = getDirection();
        if (axis != null && axis.length > 1) {
            ret = new Vector2(axis[1], -axis[0]).nor();
        }
        return ret;
    }

    public abstract int getCount();

    public abstract List<RollCell> bindCells();

    public abstract RollCell bindNextCell(RollCell rollCell);

    public synchronized void notifyDataChanged(boolean toReflesh) {
        for (RollDataChangedListener listener : mChangedListeners) {
            listener.dataChanged(toReflesh);
        }
    }

    public synchronized void addDataChangedListener(RollDataChangedListener dataChangedListener) {
        if (dataChangedListener != null && !mChangedListeners.contains(dataChangedListener)) {
            mChangedListeners.add(dataChangedListener);
        }
    }

    public synchronized void removeDataChangedListener(
            RollDataChangedListener dataChangedListener) {
        if (dataChangedListener != null) {
            mChangedListeners.remove(dataChangedListener);
        }
    }

    public synchronized void removeDataChangedListeners() {
        if (!mChangedListeners.isEmpty()) {
            mChangedListeners.clear();
        }
    }

    public interface RollDataChangedListener {
        public void dataChanged(boolean toReflesh);
    }
}
