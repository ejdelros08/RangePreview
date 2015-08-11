package com.optimind.studiotest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.optimind.studiotest.views.RangeSeekBar;
import com.optimind.studiotest.views.RangeSeekBar.OnRangeSeekBarChangeListener;

import java.io.File;

import ejdelrosario.framework.utilities.DialogUtil;
import ejdelrosario.framework.utilities.FileUtil;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private VideoView mVideoView;
    private TextView min, max;
    private LinearLayout llContainer;
    private RangeSeekBar<Integer> range;
    private Handler rangeSeekerHandler, videoPlayerHandler;
    private Runnable rangeSeekerWork, videoPlayerWork;
    private Uri mVideoURI = null;
    private FFmpeg ffmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

    }

    private void initialize(){

        ffmpeg = FFmpeg.getInstance(this);

        try {
            ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.e(TAG, "ffmpeg onFailure");
                }

                @Override
                public void onSuccess() {
                    Log.e(TAG, "ffmpeg onSuccess");
                }

                @Override
                public void onStart() {
                    Log.e(TAG, "ffmpeg onStart");
                }

                @Override
                public void onFinish() {
                    Log.e(TAG, "ffmpeg onFinish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
            Log.e(TAG, "ffmpeg not supported exception");
        }

        mVideoView = (VideoView) findViewById(R.id.video_view);
        min = (TextView) findViewById(R.id.tv_min);
        max = (TextView) findViewById(R.id.tv_max);
        llContainer = (LinearLayout) findViewById(R.id.ll_container);

        rangeSeekerHandler = new Handler();
        videoPlayerHandler = new Handler();

        videoPlayerWork = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if(mVideoView.getCurrentPosition() >= range.getSelectedMaxValue() * 1000){
                    Log.i(TAG, "current position: " + mVideoView.getCurrentPosition() + " max value: " + range.getSelectedMaxValue());
                    mVideoView.pause();
                }
                else{
                    videoPlayerHandler.postDelayed(this, 100);
                }
            }
        };

        findViewById(R.id.btn_get).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                i.setType("video/*");
                startActivityForResult(i, 800);
            }
        });

        findViewById(R.id.btn_trim).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoURI != null) {
                    new AsyncTask<Void, Void, Void>() {

                        private ProgressDialog dlg;
                        private boolean error = false;

                        protected void onPreExecute() {
                            dlg = new ProgressDialog(MainActivity.this);
                            dlg.setCancelable(false);
                            dlg.setMessage("Trimming video...");
                            dlg.show();
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            // TODO Auto-generated method stub

                            try {
                                String sourceFile = FileUtil.getPath(MainActivity.this, mVideoURI);
                                String outputFile = Environment.getExternalStorageDirectory() + "/Android/data/output." + sourceFile.substring(sourceFile.length() - 3);
                                int start = Integer.parseInt(min.getText().toString());
                                int end = Integer.parseInt(max.getText().toString());
                                int duration = end - start;

                                if (sourceFile.contains(" ")) {
                                    int lastSlashIndex = 0;
                                    for (int ctr = 0; ctr < sourceFile.length(); ctr++) {
                                        if (sourceFile.charAt(ctr) == '/') {
                                            lastSlashIndex = ctr;
                                        }
                                    }
                                    String inputFileName = sourceFile.substring(lastSlashIndex + 1, sourceFile.length());
                                    String inputFilePath = sourceFile.substring(0, lastSlashIndex + 1);
                                    Log.i(TAG, "inputFilePath: " + inputFilePath);
                                    Log.i(TAG, "inputFileName: " + inputFileName);
                                    File from = new File(sourceFile);
                                    File to = new File(new File(inputFilePath), inputFileName.replace(" ", "_"));
                                    boolean status = from.renameTo(to);
                                    Log.i(TAG, "rename status: " + status);
                                    if (status) {
                                        sourceFile = inputFilePath + inputFileName.replace(" ", "_");
                                    }
                                }


                                final String cmd = "-i " + sourceFile + " -ss " + start + " -t " + duration + " -c copy " + outputFile;
                                Log.i(TAG, "cmd: " + cmd);

                                ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                                    @Override
                                    public void onSuccess(String s) {
                                        DialogUtil.showAlertDialog(MainActivity.this, s);
                                    }

                                    @Override
                                    public void onProgress(String s) {
                                        Log.i(TAG, "ffmpeg execute onProgress: " + s);
                                    }

                                    @Override
                                    public void onFailure(String s) {
                                        DialogUtil.showAlertDialog(MainActivity.this, s);
                                    }

                                    @Override
                                    public void onStart() {
                                        Log.i(TAG, "ffmpeg execute on start");
                                    }

                                    @Override
                                    public void onFinish() {
                                        Log.i(TAG, "ffmpeg execute on finish");
                                    }
                                });
                            } catch (FFmpegCommandAlreadyRunningException e) {
                                e.printStackTrace();
                                Log.e(TAG, "ffmpeg already running exception!");
                            } catch (Exception e) {
                                e.printStackTrace();
                                error = true;
                            }

                            return null;
                        }

                        protected void onPostExecute(Void result) {
                            dlg.dismiss();
                            if (error) {
                                DialogUtil.showAlertDialog(MainActivity.this, "Error!");
                            }
                            //display trimmed video
                        }
                    }.execute();
                }
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

    private void prepareVideo(Uri uri){

        mVideoURI = uri;
        mVideoView.setVideoURI(mVideoURI);
        mVideoView.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub
                int milis = mp.getDuration();
                int seconds = (milis / 1000);
                int millis2 = 0;
                Cursor cursor = MediaStore.Video.query(MainActivity.this.getContentResolver(), mVideoURI, new String[]{MediaStore.Video.VideoColumns.DURATION});
                if (cursor.moveToFirst()) {
                    millis2 = Integer.parseInt(cursor.getString(0));
                }
                Toast.makeText(MainActivity.this, "query millis: " + millis2 + " mpMilis: " + milis + " seconds: " + seconds, Toast.LENGTH_LONG).show();
                range = new RangeSeekBar<Integer>(0, seconds, MainActivity.this);
                range.setNotifyWhileDragging(true);
                min.setText("" + 0);
                max.setText("" + seconds);

                if (seconds > 30) {
                    max.setText("" + 30);
                    range.setSelectedMaxValue(30);
                }

                range.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {

                    @Override
                    public void onRangeSeekBarValuesChanged(
                            RangeSeekBar<?> bar, Integer minValue,
                            Integer maxValue, RangeSeekBar.Thumb selectedThumb) {
                        // TODO Auto-generated method stub
                        if (maxValue - minValue > 30) {

                            if (selectedThumb != null) {

                                if (selectedThumb == RangeSeekBar.Thumb.MIN) {
                                    range.setSelectedMaxValue(minValue + 30);
                                } else {
                                    range.setSelectedMinValue(maxValue - 30);
                                }

                            }

                        }
                        min.setText("" + minValue);
                        max.setText("" + maxValue);

                        final int previewStartTime = minValue;

                        mVideoView.pause();
                        rangeSeekerHandler.removeCallbacks(rangeSeekerWork);
                        videoPlayerHandler.removeCallbacks(videoPlayerWork);
                        rangeSeekerHandler.postDelayed(rangeSeekerWork = new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mVideoView.seekTo(previewStartTime * 1000);
                                mVideoView.start();
                                videoPlayerHandler.post(videoPlayerWork);
                            }
                        }, 800);
                    }
                });

                llContainer.addView(range, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

                mVideoView.start();

                videoPlayerHandler.post(videoPlayerWork);
            }
        });

        mVideoView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (!mVideoView.isPlaying()) {
                            mVideoView.seekTo(range.getSelectedMinValue() * 1000);
                            mVideoView.start();
                            videoPlayerHandler.post(videoPlayerWork);
                        } else {
                            videoPlayerHandler.removeCallbacks(videoPlayerWork);
                            mVideoView.pause();
                        }
                        break;
                    }
                }
                return true;
            }
        });

    }
}
