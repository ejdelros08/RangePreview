package com.optimind.studiotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by EJ Del Rosario
 * Date: 7/28/15
 * Copyright (c) 2015 Optimind Technology Solutions
 * All Rights Reserved
 */
public class MenuScreen extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);

        findViewById(R.id.btn_range_seek_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchScreen(MainActivity.class);
            }
        });

        findViewById(R.id.btn_range_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchScreen(RangePreviewScreen.class);
            }
        });
    }

    private void switchScreen(Class<?> cls){
        Intent i = new Intent(this, cls);
        startActivity(i);
    }
}
