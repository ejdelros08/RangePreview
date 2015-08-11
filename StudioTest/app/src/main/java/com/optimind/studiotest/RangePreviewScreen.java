package com.optimind.studiotest;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;

import com.optimind.studiotest.views.RangePreview;

/**
 * Created by EJ Del Rosario
 * Date: 7/29/15
 * Copyright (c) 2015 Optimind Technology Solutions
 * All Rights Reserved
 */
public class RangePreviewScreen extends Activity {

    private static final String TAG = RangePreviewScreen.class.getSimpleName();

    private VideoView mVideoView;
    private RangePreview mRangePreview;
    private Uri mVideoURI;
    private TextView tvMin, tvMax, tvDuration;
    private EditText etMaxDuration, etNumThumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_range_preview_screen);

        initialize();
    }

    private void initialize() {

        mVideoView = (VideoView) findViewById(R.id.video_view);
        tvMin = (TextView) findViewById(R.id.tv_min);
        tvMax = (TextView) findViewById(R.id.tv_max);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        etMaxDuration = (EditText) findViewById(R.id.et_max_duration);
        etNumThumb = (EditText) findViewById(R.id.et_num_thumb);

        mRangePreview = (RangePreview) findViewById(R.id.range_preview);
        mRangePreview.setVisibility(View.INVISIBLE);


        findViewById(R.id.btn_get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                i.setType("video/*");
                startActivityForResult(i, 800);
            }
        });
    }

    private void prepareVideo(Uri uri){

        this.mVideoURI = uri;
        mVideoView.setVideoURI(mVideoURI);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.start();

                mRangePreview.setVisibility(View.VISIBLE);
                mRangePreview.setVideoURI(mVideoURI);
                mRangePreview.setMaxDuration(Integer.parseInt(etMaxDuration.getText().toString()));
                mRangePreview.setNumberOfThumbnailsWithinViewBounds(Integer.parseInt(etNumThumb.getText().toString()));
                mRangePreview.build();

                tvMin.setText("" + mRangePreview.getSelectedStartValue());
                tvMax.setText("" + mRangePreview.getSelectedEndValue());
                tvDuration.setText("" + mRangePreview.getSelectedDuration());

                mRangePreview.setOnRangeChangeListener(new RangePreview.OnRangeChangeListener() {
                    @Override
                    public void onRangeChange(int startValue, int endValue, int duration) {
                        tvMin.setText("" + startValue);
                        tvDuration.setText("" + duration);
                        tvMax.setText("" + endValue);
                    }
                });

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){

            case 800:{
                if(resultCode == RESULT_OK){
                    if(data.getData() != null)
                        prepareVideo(data.getData());
                    else
                        Log.i(TAG, "intent data null!");
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRangePreview.release();
    }
}
