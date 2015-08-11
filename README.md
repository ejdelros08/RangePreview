# RangePreview

<b>RangePreview</b> is a <i>Custom View</i> which acts as a range selector within a specified video showing frames from the video source on each computed interval; quite similar to Instagram's video trimmer.

I've searched for a library like https://github.com/itsmeichigo/ICGVideoTrimmer on android but I can't find any so i've tried to at least create my own implementation and I want to share it.
It's not as good as intstagram's or this library above. But I think this will do at least for the purpose I needed on my app.</br></br>
You might have a greater idea for this implementation or to at least optimize so this project is open for improvements. Or if there are existing libraries out there which has the same purpose and function, suggesting them would be great.</br></br>
<i>The uploaded project is my actual test project. Just get the RangePreview.java at /StudioTest/app/src/main/java/com/optimind/studiotest/views/RangePreview.java</i></br></br>


<b>Screenshot:</b></br>
![Alt text](https://github.com/ejdelros08/RangePreview/blob/master/sample.png)


<b>How to use:</b>

Copy the ```RangePreview.java``` to your project.

Just add to your XML
```xml
<com.optimind.studiotest.views.RangePreview
        android:id="@+id/range_preview"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight=".2" />
```

Find the view then set arguments:
```java
...

private RangePreview mRangePreview;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        mRangePreview = (RangePreview) findViewById(R.id.range_preview);
        mRangePreview.setVideoURI(mVideoURI);
        mRangePreview.setMaxDuration(30);
        mRangePreview.setNumberOfThumbnailsWithinViewBounds(7);
        mRangePreview.setOnRangeChangeListener(new RangePreview.OnRangeChangeListener() {
                    @Override
                    public void onRangeChange(int startValue, int endValue, int duration) {
                        //TODO
                    }
                });
        mRangePreview.build();        
        
        //Getting Selected Values
        int startValue = mRangePreview.getSelectedStartValue();
        int endValue = mRangePreview.getSelectedEndValue();
        int duration = mRangePreview.getSelectedDuration();
}
...

//Release when leaving the screen or layout where this Custom View is attached.
@Override
protected void onDestroy() {
        super.onDestroy();
        mRangePreview.release();
}
```

I believe the Custom View class is well-documented. Just explore and read to see other public methods available. Thanks
