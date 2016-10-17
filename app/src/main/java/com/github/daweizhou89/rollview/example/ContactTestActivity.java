
package com.github.daweizhou89.rollview.example;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.github.daweizhou89.rollview.ContactRoolView;

public class ContactTestActivity extends Activity {

    private View mRollView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRollView = new ContactRoolView(this);
        mRollView.setBackgroundColor(Color.BLACK);
        setContentView(mRollView);
    }
}
