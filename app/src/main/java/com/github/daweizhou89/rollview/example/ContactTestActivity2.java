
package com.github.daweizhou89.rollview.example;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.github.daweizhou89.rollview.ColorfulContactRoolView;


public class ContactTestActivity2 extends Activity {

    private View mRollView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRollView = new ColorfulContactRoolView(this);
        mRollView.setBackgroundColor(Color.argb(189, 226, 207, 255));
        setContentView(mRollView);
    }
}
