package com.optimind.studiotest.views;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;

import com.optimind.studiotest.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Written by <b>EJ Del Rosario</b><br>
 * Date: 7/28/15<br><br>
 *
 * <b>RangePreview</b> is a <i>Custom View</i> which acts as a range selector within a specified video showing frames from the video source on each computed interval.<br>
 * Just add this view to your XML and specify the width and height<br>
 * <i>It is recommended for the width to be ALWAYS greater than the height</i><br><br>
 * <b>Required Permissions:</b> (used for caching)<br>
 * <i><a href="http://developer.android.com/reference/android/Manifest.permission.html#WRITE_EXTERNAL_STORAGE">android.permission.WRITE_EXTERNAL_STORAGE</a></i><br>
 * <i><a href="http://developer.android.com/reference/android/Manifest.permission.html#READ_EXTERNAL_STORAGE">android.permission.READ_EXTERNAL_STORAGE</a></i><br>
 * <br>
 * <b>Public Methods:</b><br>
 * {@link #setVideoURI(Uri)}<br>
 * {@link #setMaxDuration(int)}<br>
 * {@link #setNumberOfThumbnailsWithinViewBounds(int)}<br>
 * {@link #setOnRangeChangeListener(OnRangeChangeListener)}<br>
 * {@link #setBackupCacheDirectory(String)}<br>
 * {@link #getBackupCacheDirectory()}<br>
 * {@link #getSelectedStartValue()}<br>
 * {@link #getSelectedDuration()}<br>
 * {@link #getSelectedEndValue()}<br>
 * {@link #build()}<br>
 * {@link #release()}
 */
public class RangePreview extends View {

    private Uri mVideoURI;
    private Context mContext;
    private MediaMetadataRetriever metadata;

    private OnRangeChangeListener mListener;
    private int startValue, endValue, duration;

    private Bitmap selector, thumbnail;

    private int videoDuration;
    private float selectionRectangleRight, selectionRectangleLeft;
    private float maxRangeDuration;
    private int numberOfThumbnails = 7;//7 as default value
    private boolean isRightSelectorOnDragMode = false;
    private boolean isLeftSelectorOnDragMode = false;
    private String backupCacheDir = Environment.getExternalStorageDirectory() + "/Android/data/RPDiskCache/";
    private File backupCacheFileDirectory;
    private BitmapFactory.Options options;

    private float dragOrigin = 0;
    private float dragDifference = 0;
    private float lastPosition = 0;
    private int numberOfBitmap = 0;

    private RectF selectionRectangleRange, videoThumbnailsRectangleBounds;
    private RectF leftSelectorRectangleBounds, leftUnselectedRange;
    private RectF rightSelectorRectangleBounds, rightUnselectedRange;
    private RectF thumbnailRectangleBounds, thumbnailCropBounds;
    private Matrix mMatrix;

    private final Paint selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ArrayList<ThumbnailHandler> thumbnails = new ArrayList<ThumbnailHandler>();
    private LruCache<String, Bitmap> memoryCache;
    private FromDiskToCacheTask mDiskToCache;

    private boolean hasFinishedBuilding = false;
    private boolean isReleasing = false;


    public RangePreview(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public RangePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public RangePreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init(){

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 2;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                if(!isReleasing) {
                    backupBitmapForCache(key, oldValue);
                }
            }
        };

        selector = BitmapFactory.decodeResource(getResources(), R.drawable.img_selector);
        videoThumbnailsRectangleBounds = new RectF();
        selectionRectangleRange = new RectF();
        leftSelectorRectangleBounds = new RectF();
        leftUnselectedRange = new RectF();
        rightUnselectedRange = new RectF();
        rightSelectorRectangleBounds = new RectF();
        thumbnailRectangleBounds = new RectF();
        thumbnailCropBounds = new RectF();
        mMatrix = new Matrix();

        backupCacheFileDirectory = new File(backupCacheDir);
        if(!backupCacheFileDirectory.exists()){
            backupCacheFileDirectory.mkdirs();
        }
        else{
            deleteBackupCacheDirectory();
            backupCacheFileDirectory.mkdirs();
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        mDiskToCache = new FromDiskToCacheTask();

    }

    private void deleteBackupCacheDirectory(){

        File[] files = backupCacheFileDirectory.listFiles();
        if (files == null) {
            backupCacheFileDirectory.delete();
        }
        for(int i=0; i<files.length; i++) {
            files[i].delete();
        }
        backupCacheFileDirectory.delete();

    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    private void backupBitmapForCache(String key, Bitmap bmp){

        try {
            File file = new File(backupCacheDir, key);
            if (!file.exists()) {
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 70, fos);
                fos.flush();
                fos.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBackupBitmap(String key){
        String path = backupCacheDir + key;
        File file = new File(path);
        if(file.exists()){
            return BitmapFactory.decodeFile(path, options);
        }
        else{
            return null;
        }
    }

    private void getBitmapfromDiskToCache(int index){

        if(mDiskToCache.getStatus() != AsyncTask.Status.RUNNING) {
            mDiskToCache.execute(index);
        }
    }

    /**
     * Sets the video source
     * @param uri video srouce URI
     */
    public void setVideoURI(Uri uri){
        this.mVideoURI = uri;
        //get video duration
        Cursor cursor = MediaStore.Video.query(mContext.getContentResolver(), mVideoURI, new String[]{MediaStore.Video.VideoColumns.DURATION});
        if (cursor.moveToFirst()) {
            this.videoDuration = Integer.parseInt(cursor.getString(0)) + 1000;// + 1 second to include 0 seconds in the range
        }
        metadata = new MediaMetadataRetriever();
        metadata.setDataSource(mContext, mVideoURI);
    }

    /**
     * sets the maxDuration value for the given layout width of this view.<br>
     * Must be called AFTER {@link #setVideoURI(Uri)}<br>
     * note that smaller maxDuration will create more thumbnail preview thus memory issues may occur.<br>
     * See {@link #setNumberOfThumbnailsWithinViewBounds(int)}
     * @param maxDuration maxDuration in seconds
     */
    public void setMaxDuration(int maxDuration){
        if((maxDuration * 1000) > videoDuration){
            videoDuration -= 1000;//remove +1 when actual video duration is less than desired max duration
            maxRangeDuration = (videoDuration / 1000);
        }
        else {
            maxRangeDuration = maxDuration;
        }
        maxRangeDuration += 1f;// + 1 second to include 0 seconds in the range
        //set default values to half of the range
        endValue = (int) (maxRangeDuration - 1) / 2;
        duration = (int) (maxRangeDuration - 1) / 2;
        selectionRectangleRight = getWidth() / 2;
    }

    /**
     * sets the number of thumbnails to be drawn within the given bounds.<br>
     * Default value is 7. Call this before {@link #build()}.
     * @param num number of thumbnails
     */
    public void setNumberOfThumbnailsWithinViewBounds(int num){
        this.numberOfThumbnails = num;
    }

    /**
     * sets the backup cache directory to save thumbnails.<br>
     * <i>Default location: Environment.getExternalStorageDirectory() + "/Android/data/RPDiskCache/";</i>
     * @param cacheDir full path to save cache
     */
    public void setBackupCacheDirectory(String cacheDir){
        this.backupCacheDir = cacheDir;
    }

    /**
     * Default location: <i>Environment.getExternalStorageDirectory() + "/Android/data/RPDiskCache/";</i><br>
     * <i>See</i> {@link #setBackupCacheDirectory(String)}
     * @return current cache directory
     */
    public String getBackupCacheDirectory(){
        return this.backupCacheDir;
    }

    /**
     * sets the onRangeChangeListener. Listener occurs on selector drag or on thumbnails drag
     * @param listener listener instance
     */
    public void setOnRangeChangeListener(OnRangeChangeListener listener){
        this.mListener = listener;
    }

    /**
     *
     * @return startValue of the selected range in seconds
     */
    public int getSelectedStartValue(){
        return this.startValue;
    }

    /**
     *
     * @return endValue of the selected range in seconds
     */
    public int getSelectedEndValue(){
        return this.endValue;
    }

    /**
     *
     * @return duration of the selected range in seconds
     */
    public int getSelectedDuration(){
        return this.duration;
    }

    /**
     * Creates set of thumbnails to be populated on the layout.
     * Must be called when all necessary parameters are set.
     * Call {@link #release()} when leaving the layout where this view is attached
     */
    public void build(){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                int thumbnailWidth = getWidth() / numberOfThumbnails;

                int videoRectangleLength = getPixelsPerSecond() * convertMillisToSeconds(videoDuration);
                while(true){
                    if(thumbnailWidth * numberOfBitmap > videoRectangleLength){
                        numberOfBitmap -= 1;
                        break;
                    }
                    else{
                        numberOfBitmap += 1;
                    }
                }

                //convert millis to micro second
                long interval = (videoDuration * 1000) / numberOfBitmap;

                for(int ctr = 0; ctr < numberOfBitmap; ctr ++){
                    long frame = interval * ctr;
                    ThumbnailHandler handler = new ThumbnailHandler();
                    handler.setFrame(frame);
                    handler.setCacheName(ctr + "_" + System.currentTimeMillis() + ".png");
                    thumbnails.add(handler);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hasFinishedBuilding = true;
                invalidate();
            }
        }.execute();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:{
                if(!isRightSelectorOnDragMode) {
                    if(rightSelectorRectangleBounds.contains(event.getX(), event.getY())){
                        isRightSelectorOnDragMode = true;
                    }
                    else if(leftSelectorRectangleBounds.contains(event.getX(), event.getY())){
                        isLeftSelectorOnDragMode = true;
                    }
                    else{
                        dragOrigin = event.getX();
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE:{
                if(isRightSelectorOnDragMode){
                    //prevent from overlapping other selector
                    if(event.getX() - (getWidth() * .07f) >= leftSelectorRectangleBounds.right) {
                        selectionRectangleRight = event.getX();
                    }
                }
                else if(isLeftSelectorOnDragMode){
                    //prevent from overlapping other selector
                    if(event.getX() + (getWidth() * .07f) <= rightSelectorRectangleBounds.left) {
                        selectionRectangleLeft = event.getX();
                    }
                }
                else{
                    float tmpDifference = event.getX() - dragOrigin;
                    //limit pan within the given width
                    if(lastPosition + tmpDifference < 1 && (getPixelsPerSecond() * convertMillisToSeconds(videoDuration)) + (lastPosition + tmpDifference) >= getWidth()){
                        dragDifference = event.getX() - dragOrigin;
                    }
                }
                invalidate();
                trackProgress();
                if(mListener != null) {
                    mListener.onRangeChange(startValue, endValue, duration);
                }
                break;
            }

            case MotionEvent.ACTION_UP:{
                if(isRightSelectorOnDragMode) {
                    isRightSelectorOnDragMode = false;
                }
                else if(isLeftSelectorOnDragMode){
                    isLeftSelectorOnDragMode = false;
                }
                else {
                    lastPosition = lastPosition + dragDifference;
                    dragDifference = 0;
                }
                invalidate();
                break;
            }
        }

        return true;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        selectionPaint.setColor(Color.BLACK);
        videoThumbnailsRectangleBounds.set(lastPosition + dragDifference, 0, (getPixelsPerSecond() * convertMillisToSeconds(videoDuration)) + (lastPosition + dragDifference), getHeight());
        canvas.drawRect(videoThumbnailsRectangleBounds, selectionPaint);

        if(hasFinishedBuilding) {
            //draw bitmap tiles
            for (int ctr = 0; ctr < numberOfBitmap; ctr++) {

                int interval = (int) videoThumbnailsRectangleBounds.width() / numberOfBitmap;

                float rectFLeft = videoThumbnailsRectangleBounds.left + (interval * ctr);
                float rectFTop = 0;
                float rectFRight = videoThumbnailsRectangleBounds.left + (interval * (ctr + 1));
                float rectFBot = videoThumbnailsRectangleBounds.height();
                thumbnailRectangleBounds.set(rectFLeft, rectFTop, rectFRight, rectFBot);

                //draw bitmap only when bounds is within the view
                if(thumbnailRectangleBounds.left >= 0){
                    if(thumbnailRectangleBounds.left < getWidth()){
                        thumbnail = getBitmapFromMemCache(thumbnails.get(ctr).getCacheName());
                        //draw
                        if (thumbnail != null) {
                            int rectLeft = thumbnail.getWidth() / 4;
                            int rectTop = 0;
                            int rectRight = (thumbnail.getWidth() / 2) + rectLeft;
                            int rectBot = thumbnail.getHeight();
                            thumbnailCropBounds.set(rectLeft, rectTop, rectRight, rectBot);
                            mMatrix.setRectToRect(thumbnailCropBounds, thumbnailRectangleBounds, Matrix.ScaleToFit.CENTER);
                            canvas.drawBitmap(thumbnail, mMatrix, null);

                        }
                        else {
                            getBitmapfromDiskToCache(ctr);
                        }
                    }
                    else{
                        thumbnail = null;
                    }
                }
                else{
                    if(thumbnailRectangleBounds.right > 0){
                        thumbnail = getBitmapFromMemCache(thumbnails.get(ctr).getCacheName());
                        //draw
                        if (thumbnail != null) {
                            int rectLeft = thumbnail.getWidth() / 4;
                            int rectTop = 0;
                            int rectRight = (thumbnail.getWidth() / 2) + rectLeft;
                            int rectBot = thumbnail.getHeight();
                            thumbnailCropBounds.set(rectLeft, rectTop, rectRight, rectBot);
                            mMatrix.setRectToRect(thumbnailCropBounds, thumbnailRectangleBounds, Matrix.ScaleToFit.CENTER);
                            canvas.drawBitmap(thumbnail, mMatrix, null);
                        }
                        else {
                            getBitmapfromDiskToCache(ctr);
                        }
                    }
                    else{
                        thumbnail = null;
                    }
                }
            }
        }

        selectionPaint.setColor(Color.BLUE);
        selectionPaint.setStrokeWidth(getHeight() * .05f);//get 5% of the view height as the stroke width
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setAntiAlias(true);
        selectionRectangleRange.set(selectionRectangleLeft, 0, selectionRectangleRight, getHeight());
        canvas.drawRect(selectionRectangleRange, selectionPaint);


        rightUnselectedRange.set(selectionRectangleRange.right, 0, getWidth(), getHeight());
        selectionPaint.setColor(Color.WHITE);
        selectionPaint.setAlpha(100);
        selectionPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rightUnselectedRange, selectionPaint);

        leftUnselectedRange.set(0, 0, selectionRectangleRange.left, getHeight());
        canvas.drawRect(leftUnselectedRange, selectionPaint);

        rightSelectorRectangleBounds.set(selectionRectangleRange.right - (getHeight() * .07f), 0, selectionRectangleRange.right + (getHeight() * .05f), getHeight());
        canvas.drawBitmap(selector, null, rightSelectorRectangleBounds, null);

        leftSelectorRectangleBounds.set(selectionRectangleRange.left - (getHeight() * .05f), 0, selectionRectangleRange.left + (getHeight() * .07f), getHeight());
        canvas.drawBitmap(selector, null, leftSelectorRectangleBounds, null);

    }

    private int getPixelsPerSecond(){
        return  (int) (getWidth() * (1 / (maxRangeDuration - 1)));
    }

    private void trackProgress(){
        float leftValue = videoThumbnailsRectangleBounds.left;
        //convert to positive value
        if(leftValue < 0){
            leftValue = leftValue * (-1f);
        }

        startValue = (int) ((leftValue + selectionRectangleRange.left) / getPixelsPerSecond());

        duration = (int) (selectionRectangleRange.width() / getPixelsPerSecond()) + 1;// + 1 second to include 0 second on the range

        endValue = startValue + duration;

    }

    public interface OnRangeChangeListener{
        void onRangeChange(int startValue, int endValue, int duration);
    }

    private int convertMillisToSeconds(int videoDuration){
        return videoDuration / 1000;
    }

    private class ThumbnailHandler{

        private long frame;
        private String cacheName;

        public long getFrame() {
            return frame;
        }

        public void setFrame(long frame) {
            this.frame = frame;
        }

        public String getCacheName() {
            return cacheName;
        }

        public void setCacheName(String cacheName) {
            this.cacheName = cacheName;
        }
    }

    private class FromDiskToCacheTask extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... params) {
            int index = params[0];
            String key = thumbnails.get(index).getCacheName();
            Bitmap bmp = getBackupBitmap(key);
            if(bmp != null){
                addBitmapToMemoryCache(key, bmp);
            }
            else{
                addBitmapToMemoryCache(key, metadata.getFrameAtTime(thumbnails.get(index).getFrame(), MediaMetadataRetriever.OPTION_CLOSEST));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mDiskToCache = new FromDiskToCacheTask();
            invalidate();
        }
    }

    /**
     * Call this when leaving the screen or layout where this Custom View is attached.
     */
    public void release(){
        isReleasing = true;
        memoryCache.evictAll();
        deleteBackupCacheDirectory();
    }

}
