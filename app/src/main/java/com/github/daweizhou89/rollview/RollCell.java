package com.github.daweizhou89.rollview;

import android.graphics.Bitmap;
import android.graphics.Color;

public class RollCell {
    public static final int TYPE_NAME = 0;
    public static final int TYPE_IMG = 0;
    public static final int DEFAULT_ALPHA = 0xFF;

    public int type;
    public Object obj;
    public int color = Color.BLACK;
    public Bitmap buffer;
    public String name = "";
    public float x;
    public float y;
    public float z;
    public int alpha = DEFAULT_ALPHA;
    public float width;
    public float heigth;
    public boolean nextChanged = true;
    public float[] matrixValue = new float[9];
    Bitmap cache;

    public void reset() {
        type = TYPE_NAME;
        obj = null;
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
        alpha = 0xFF;
        name = "";
        width = 0.0f;
        heigth = 0.0f;
    }

    public void set(float px, float py, float pz) {
        this.x = px;
        this.y = py;
        this.z = pz;
    }
}
