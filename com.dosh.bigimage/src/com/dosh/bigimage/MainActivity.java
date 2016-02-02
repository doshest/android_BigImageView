
package com.dosh.bigimage;

import com.dosh.bigimage.model.PicInfo;
import com.dosh.bigimage.model.Picture;
import com.dosh.bigimage.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint({
        "NewApi", "ClickableViewAccessibility"
})
public class MainActivity extends Activity implements IDownloadState {

    BigImageView image = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (BigImageView)findViewById(R.id.bigimage);
        Picture p = new Picture();
        System.out.println(Utils.getPictureImgSaveDir(this));
        p.setPicInfo(new PicInfo());
        p.setLocalPath(Utils.getPictureImgSaveDir(this));
        image.setImageUrl(p, this);

        // image.setong
        final GestureDetector detector = new GestureDetector(this, new GestureListener());

        image.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub

                return detector.onTouchEvent(event);
            }
        });

    }

    private class GestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            // ²¶»ñDownÊÂ¼þ
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // TODO Auto-generated method stub
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // TODO Auto-generated method stub
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            image.scrollBy(0, (int)distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // TODO Auto-generated method stub
            // image.onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // TODO Auto-generated method stub
            super.onShowPress(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // TODO Auto-generated method stub
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // TODO Auto-generated method stub
            return super.onSingleTapConfirmed(e);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProgressChanged(float percent) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onComplete(Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFail(Object arg) {
        // TODO Auto-generated method stub

    }
}
