package com.example.renpeng.jpct_demo;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by renpeng on 2017/11/23.
 */

public class Drag_Scale extends View {
    int screenHeight;
    int screenWidth;
    GestureDetector gestures;
    ScaleGestureDetector scaleGesture;
    float scale = 1.0f;
    float horizontalOffset, verticalOffset;

    int NORMAL = 0;
    int ZOOM = 1;
    int DRAG = 2;
    boolean isScaling = false;
    float touchX, touchY;
    int mode = NORMAL;

    public Drag_Scale(Context context) {
        super(context);
        //initializing variables
        Log.i("My_JPCT","Drag_Scale created");
        scaleGesture = new ScaleGestureDetector(getContext(),
                new ScaleListener());
        gestures = new GestureDetector(getContext(), new GestureListener());
        mode = NORMAL;
        initialize();
    }

    //Best fit image display on canvas
    private void initialize() {
        Log.i("My_JPCT","initialize");
        invalidate();
    }

    public class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactorNew = detector.getScaleFactor();
            if (detector.isInProgress()) {
                touchX = detector.getFocusX();
                touchY = detector.getFocusY();
                scale *= scaleFactorNew;
                Log.i("My_JPCT","scale"+scale);
                invalidate(0, 0, screenWidth, screenHeight);
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScaling = true;
            Log.i("My_JPCT","Scale begin");
            mode=ZOOM;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mode = NORMAL;
            Log.i("My_JPCT","Scale end");
            isScaling = false;
        }

    }

    public class GestureListener implements GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onDown(MotionEvent e) {
            isScaling = false;
            return true;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (!isScaling) {
                Log.i("My_JPCT","Drag");
                mode = DRAG;
                isScaling = false;
                horizontalOffset -= distanceX;
                verticalOffset -= distanceY;
                invalidate(0, 0, screenWidth, screenHeight);
            } else {
                Log.i("My_JPCT","Zoom");
                mode = ZOOM;
                isScaling = true;
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGesture.onTouchEvent(event);
        gestures.onTouchEvent(event);
        return true;
    }
}


