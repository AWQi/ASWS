package com.example.largeimage;

import android.animation.ObjectAnimator;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class OprateLargeImage {
        private static final String TAG = "MapActivity";
        private ImageView imageView = null;
        private Button vernier = null;
       public   OprateLargeImage (ImageView view) {
           imageView = view;
//            vernier = findViewById(R.id.vernier);

//            map.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    float x = motionEvent.getX();
//                    float y = motionEvent.getY();
//                    Log.d(TAG, "onTouch: x------"+x);
//                    Log.d(TAG, "onTouch: y------"+y);
//                    translateAnimation(vernier,x,y);
//                    return false;
//                }
//            });

            view.setOnTouchListener(new TouchListener());

        }
        void  translateAnimation(View view,float desX,float desY){
            ObjectAnimator animatorX = ObjectAnimator.ofFloat(view,"translationX",view.getX(),desX);
            animatorX.setDuration(500);
            animatorX.start();
            ObjectAnimator animatorY = ObjectAnimator.ofFloat(view,"translationY",view.getY(),desY);
            animatorY.setDuration(500);
            animatorY.start();

        }

        private  class TouchListener implements View.OnTouchListener{

            /** 记录是拖拉照片模式还是放大缩小照片模式 */
            private int mode = 0;// 初始状态
            private static final  int MODE_DEFAULT  = 0;
            /** 拖拉照片模式 */
            private static final int MODE_DRAG = 1;
            /** 放大缩小照片模式 */
            private static final int MODE_ZOOM = 2;
            private static final int MODE_CLICK = 3;
            private static final int MIN_DIS =  50;

            /** 用于记录开始时候的坐标位置 */
            private PointF startPoint = new PointF();
            /** 用于记录拖拉图片移动的坐标位置 */
            private Matrix matrix = new Matrix();
            /** 用于记录图片要进行拖拉时候的坐标位置 */
            private Matrix currentMatrix = new Matrix();

            /** 两个手指的开始距离 */
            private float startDis;
            /** 两个手指的中间点 */
            private PointF midPoint;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    // 手指压下屏幕
                    case MotionEvent.ACTION_DOWN:
                        mode = MODE_CLICK;
                        // 记录ImageView当前的移动位置
                        currentMatrix.set(imageView.getImageMatrix());
                        startPoint.set(event.getX(), event.getY());
                        break;
                    // 手指在屏幕上移动，改事件会被不断触发
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "onTouch:          move");
                        if (mode==MODE_CLICK){
                            mode = MODE_DRAG;
                        }
                        // 拖拉图片
                        if (mode == MODE_DRAG) {
                            float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                            float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                            // 在没有移动之前的位置上进行移动
                            matrix.set(currentMatrix);
                            matrix.postTranslate(dx, dy);
                        }
                        // 放大缩小图片
                        else if (mode == MODE_ZOOM) {
                            float endDis = distance(event);// 结束距离
                            if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                                float scale = endDis / startDis;// 得到缩放倍数
                                matrix.set(currentMatrix);
                                matrix.postScale(scale, scale,midPoint.x,midPoint.y);
                            }
                        }
                        break;
                    // 手指离开屏幕
                    case MotionEvent.ACTION_UP:
                        float x = event.getX();
                        float y = event.getY();
                        Log.d(TAG, "onTouch: x------"+x);
                        Log.d(TAG, "onTouch: y------"+y);


//                        translateAnimation(vernier,x,y);



                        // 当触点离开屏幕，但是屏幕上还有触点(手指)
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = 0;
                        break;
                    // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mode = MODE_ZOOM;
                        /** 计算两个手指间的距离 */
                        startDis = distance(event);
                        /** 计算两个手指间的中间点 */
                        if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                            midPoint = mid(event);
                            //记录当前ImageView的缩放倍数
                            currentMatrix.set(imageView.getImageMatrix());
                        }
                        break;
                }
                imageView.setImageMatrix(matrix);
                return true;
            }

            /** 计算两个手指间的距离 */
            private float distance(MotionEvent event) {
                float dx = event.getX(1) - event.getX(0);
                float dy = event.getY(1) - event.getY(0);
                /** 使用勾股定理返回两点之间的距离 */
                return (float) Math.sqrt(dx * dx + dy * dy);
            }

            /** 计算两个手指间的中间点 */
            private PointF mid(MotionEvent event) {
                float midX = (event.getX(1) + event.getX(0)) / 2;
                float midY = (event.getY(1) + event.getY(0)) / 2;
                return new PointF(midX, midY);
            }

        }
    }


