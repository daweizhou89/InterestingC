package com.github.daweizhou89.rollview.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.daweizhou89.interestingc.R;


public class RollViewActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rollview_test);
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RollViewActivity.this, ContactTestActivity.class));
            }
        });
        
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RollViewActivity.this, ContactTestActivity2.class));
            }
        });
    }

}
