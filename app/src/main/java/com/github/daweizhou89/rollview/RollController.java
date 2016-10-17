package com.github.daweizhou89.rollview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.Log;

import com.github.daweizhou89.rollview.math.Vector2;

public class RollController implements RollViewAdapter.RollDataChangedListener {
    boolean ENABLE_ROLL_CELL_COLOR = false;
    
    private Vector2 mDirection;
    private Vector2 mAxis;
    private int mBallRadius;
    private float mFourFifthBallRadius;
    
    /** 使用{@link RollController.setAngularVelocity} 进行操作 */
    private float mAngularVelocity = 0.0f; 
    private float mAngularAcceleration = 0.0f;
    private float mAngularAccelerationTemp = 0.0f;
    private float mSinVelocity;
    private float mCosVelocity;
    private boolean mIsUniformAndCircular;
    private double mMaxAngularVelocity;
    
    private List<RollCell> mRollCells;
    private List<RollCell> mFrontRollCells = new ArrayList<RollCell>();
    private List<RollCell> mBackRollCells = new ArrayList<RollCell>();
    private Context mContext;
    private Paint mPaint;
    
    private IRollView mRollView;
    private RollViewAdapter mAdapter;
    private String mNoDataTips = RollView.TEXT_DEFAULT_NO_DATA_TIPS;
    
    public RollController(Context context, IRollView rollView) {
        mContext = context;
        mRollView = rollView;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    }
    
    public void setBallRadius(int ballRadius) {
        if (mBallRadius != ballRadius) {
            mBallRadius = ballRadius;
            mFourFifthBallRadius = mBallRadius * 4.f / 5.f;
            layoutRollCells(mBallRadius);
        }
    }
    
    private void setAngularVelocity(float angularVelocity) {
        mAngularVelocity = angularVelocity;
        mSinVelocity = (float)Math.sin(mAngularVelocity);
        mCosVelocity = (float)Math.cos(mAngularVelocity);
    }
    
    /**
     * 布局滚动元素
     * @param ballRadius
     */
    private void layoutRollCells(int ballRadius) {
        if (!mFrontRollCells.isEmpty()) mFrontRollCells.clear();
        if (!mBackRollCells.isEmpty()) mBackRollCells.clear();
        
        if(isEmpty()) return;
        
        int rollcellCount = mRollCells.size();
        double thetaFactor = Math.sqrt(rollcellCount * Math.PI);
        for (int i = 0; i < rollcellCount; i++) {
            RollCell rollCell = mRollCells.get(i);
            
            bindNameWidthAndHeight(rollCell);
            
            float phi = (float)Math.acos(- 1.0f + (2.0f * i) / rollcellCount);
            float theta = (float) (thetaFactor * phi);
            float px = (float) (ballRadius * Math.cos(theta) * Math.sin(phi));
            float py = (float) (ballRadius * Math.sin(theta) * Math.sin(phi));
            float pz = (float) (ballRadius * Math.cos(phi));
            rollCell.set(px, py, pz);
            rollCell.alpha = (int)(Math.abs(rollCell.z - mBallRadius) * 255 / (2 * mBallRadius));
            if(rollCell.z > 0) {
                mBackRollCells.add(rollCell);
            } else {
                mFrontRollCells.add(rollCell);
            }
        }
    }
    
    private void bindNameWidthAndHeight(RollCell rollCell) {
        FontMetrics fontMetrics = mPaint.getFontMetrics();
        rollCell.width = mPaint.measureText(rollCell.name);
        rollCell.heigth = fontMetrics.bottom - fontMetrics.top;
    }
    
    /**
     * 初始化移动方向
     * @param startPosition 起始点
     * @param endPosition 结束点
     */
    public void setDirection(Vector2 startPosition, Vector2 endPosition) {
        
        if(isEmpty() || mIsUniformAndCircular) return;
        
        Vector2 direction = endPosition.sub(startPosition);

        float distance = direction.len();
        if (distance > (2 * mBallRadius)) {
            distance = (2 * mBallRadius);
        }
        float angularVelocity = (float) (distance / (2.f * mBallRadius) * mMaxAngularVelocity);
        setAngularVelocity(angularVelocity);

        mDirection = direction.nor();
        mAxis = mDirection.normalVector();
    }
    
    /**
     * 设定角加速度
     * @param angularAcceleration
     */
    public void setAngularAcceleration(float angularAcceleration) {
        mAngularAcceleration = angularAcceleration;
    }
    
    public void restoreAngularAcceleration() {
        if (mAdapter != null) {
            mAngularAcceleration = mAdapter.getAngularAcceleration();
        }
    }

    /**
     * 滚动
     */
    public void roll() {
        if(isEmpty() || mAngularVelocity <= 0.0f || mAxis == null) {
            return;
        }
        if (!mFrontRollCells.isEmpty()) mFrontRollCells.clear();
        if (!mBackRollCells.isEmpty()) mBackRollCells.clear();
        for (int i = 0; i < mRollCells.size(); i++) {
            RollCell rollCell = mRollCells.get(i);
            float ax = mAxis.x;
            float ay = mAxis.y;
            
            float px = rollCell.x;
            float py = rollCell.y;
            float pz = rollCell.z;
            
            float psx = px * (ax * ax * (1 - mCosVelocity) + mCosVelocity) + py * ax * ay * (1 - mCosVelocity) + pz * ay * mSinVelocity;
            float psy = px * ax * ay * (1 - mCosVelocity) + py * (ay * ay * (1 - mCosVelocity) + mCosVelocity) - pz * ax * mSinVelocity;
            float psz = -px * ay * mSinVelocity + py * ax * mSinVelocity + pz * mCosVelocity;
            
            rollCell.set(psx, psy, psz);
            
            rollCell.alpha = (int)(Math.abs(rollCell.z - mBallRadius) * 255 / (2 * mBallRadius));
            
            if(rollCell.z > 0) {
                mBackRollCells.add(rollCell);
                if(rollCell.z > mFourFifthBallRadius) {
                    if (!rollCell.nextChanged) {
                        mAdapter.bindNextCell(rollCell);
                        bindNameWidthAndHeight(rollCell);
                        rollCell.nextChanged = true;
                    }
                } else {
                    rollCell.nextChanged = false;
                }
            } else {
                mFrontRollCells.add(rollCell);
            }
        }
        if(mAngularAcceleration != 0.0f) {
            setAngularVelocity(mAngularVelocity + mAngularAcceleration);
        }
    }
    
    /**
     * 绘制
     * @param canvas
     * @param camera
     * @param matrix
     * @param paint
     */
    public void draw(Canvas canvas, Camera camera, Matrix matrix) {
        if(isEmpty()) {
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * 4);
            canvas.save();
            String text = mNoDataTips;
            FontMetrics fontMetrics = mPaint.getFontMetrics();
            float textWidth = mPaint.measureText(text);
            float textHeight = fontMetrics.bottom - fontMetrics.top;
            canvas.translate(- textWidth / 2, - textHeight / 2);
            canvas.drawText(text, 0, 0, mPaint);
            canvas.restore();
            mPaint.setTextSize(textSize);
        } else {
            drawRollCells(canvas, camera, matrix, mPaint);
        }
    }
    
    private void drawRollCells(Canvas canvas, Camera camera, Matrix matrix, Paint paint) {
        int lastAlpha = paint.getAlpha();
        for (RollCell rollCell : mBackRollCells) {
            drawRollCell(rollCell, canvas, camera, matrix, paint);
        }
        for (RollCell rollCell : mFrontRollCells) {
            drawRollCell(rollCell, canvas, camera, matrix, paint);
        }
        paint.setAlpha(lastAlpha);
    }
    
    private void drawRollCell(RollCell rollCell, Canvas canvas, Camera camera, Matrix matrix, Paint paint) {
        canvas.save();
        camera.save();
        matrix.reset();
        camera.translate(rollCell.x, rollCell.y, rollCell.z);
        camera.getMatrix(matrix);
        camera.restore();
        
        canvas.translate(-rollCell.width / 2, -rollCell.heigth / 2);
        canvas.concat(matrix);
        
        //save the matrix' value in order to check clicking;
        matrix.getValues(rollCell.matrixValue);
        
        int color = mPaint.getColor();
        if (ENABLE_ROLL_CELL_COLOR) {
            if (rollCell.color != color)
                mPaint.setColor(rollCell.color);
        }
        paint.setAlpha(rollCell.alpha);
        canvas.drawText(rollCell.name, 0, 0, paint);
        
        //恢复绘制状态
        if (ENABLE_ROLL_CELL_COLOR) {
            mPaint.setColor(color);
        }
        canvas.restore();
    }
    
    public RollCell checkClick(float x, float y) {
        RollCell ret = null;
        if (!isEmpty()) {
            for (RollCell rollCell : mRollCells) {
                if (rollCell.z < 0) {
                    float deltaX = Math.abs(x - rollCell.matrixValue[Matrix.MTRANS_X]);
                    float deltaY = Math.abs(y - rollCell.matrixValue[Matrix.MTRANS_Y]);
                    float scale = rollCell.matrixValue[Matrix.MSCALE_X];
                    float limitedX = rollCell.width * scale / 2;
                    float limitedY = rollCell.heigth * scale / 2;
                    limitedY *= 5.f / 4.f;
                    if (deltaX < limitedX && deltaY < limitedY) {
                        Log.v("dawei", "clicked [" + x + ", " + y + "]");
                        ret = rollCell;
                        break;
                    }
                }
            }
        }
        return ret;
    }
    
    private boolean isEmpty() {
        return mRollCells == null || mRollCells.size() == 0 || mAdapter == null;
    }
    
    public boolean isToRoll() {
        return (mAngularVelocity > 0.0f && mAxis != null);
    }
    
    public void setRollViewAdapter(RollViewAdapter adapter) {
        //remove older one
        if (mAdapter != null) {
            mAdapter.removeDataChangedListener(this);
        }
        
        //set newer one
        if(mAdapter != adapter) {
            mAdapter = adapter;
            mAdapter.addDataChangedListener(this);
            bindRollData();
        }
        mRollView.refresh();
    }
    
    private void bindRollData() {
        if(mAdapter != null) {
            int radius = mAdapter.getRadius();
            if (radius > 0) {
                mBallRadius = radius;
            }
            mRollCells = mAdapter.bindCells();
            mPaint.setTextSize(mContext.getResources().getDisplayMetrics().scaledDensity
                    * mAdapter.getTextSize());
            mPaint.setColor(mAdapter.getTextColor());
            mIsUniformAndCircular = mAdapter.isUniformAndCircular();
            if (mIsUniformAndCircular) {
                mAxis = mAdapter.getAxis();
                setAngularVelocity((float) mAdapter.getAngularVelocity());
                mAngularAcceleration = 0.0f;
            } else {
                mMaxAngularVelocity = mAdapter.getMaxAngularVelocity();
                mAngularAcceleration = mAdapter.getAngularAcceleration();
            }
            layoutRollCells(mBallRadius);
        }
        if(mAdapter == null || mAdapter.getCount() <= 0) {
            mRollCells = null;
            if (!mFrontRollCells.isEmpty())
                mFrontRollCells.clear();
            if (!mBackRollCells.isEmpty())
                mBackRollCells.clear();
        }
    }

    @Override
    public void dataChanged(boolean toReflesh) {
        if (toReflesh) {
            bindRollData();
            mRollView.refresh();
        }
    }

    public void setNoDataTips(String tips) {
        if (tips != null) {
            mNoDataTips = tips;
        } else {
            mNoDataTips = RollView.TEXT_DEFAULT_NO_DATA_TIPS;
        }
        mRollView.refresh();
    }
    
    public boolean isUniformAndCircular() {
        return mIsUniformAndCircular;
    }
}
