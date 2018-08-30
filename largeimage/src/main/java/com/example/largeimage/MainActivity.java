package com.example.largeimage;

import android.animation.ObjectAnimator;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;


//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";
//    private LargeImageView mLIv;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        mLIv = (LargeImageView) findViewById(R.id.iv_large);
//        // load Assets image
//        try {
//            InputStream is = getAssets().open("a.jpg");
//            // 调用自定义大图加载View
//            mLIv.setInputStream(is);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}


public class MainActivity extends AppCompatActivity {
private ImageView show = null;
    private static final String TAG = "MainActivity";
    private  int screenHeigth;
    private  int screenWidth;
    private  int imageHeight;
    private  int imageWidth;
    private  BitmapRegionDecoder bitmapRegionDecoder;
    private  BitmapFactory.Options options;
    private  Rect rect;
    private  FrameLayout largeImageLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
         screenWidth= display.getWidth();
         screenHeigth= display.getHeight();
         largeImageLayout = findViewById(R.id.largeImageLayout);

        show  =findViewById(R.id.show);
        loadImage();
        load(show);
        new  OprateLargeImage(show);



    }



    private  void  loadImage(){
        try {
            AssetManager assetManager = getAssets();

            InputStream is=assetManager.open("a.jpg");
//            InputStream is = getAssets().open("E:\\a.jpg");
            BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
            tmpOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, tmpOptions);
            imageWidth  = tmpOptions.outWidth;
            imageHeight = tmpOptions.outHeight;
            Log.d(TAG, "onCreate:  width---------------- "+imageWidth);
            Log.d(TAG, "onCreate:  height-------------"+imageHeight);
            // 设置显示图片的中心区域
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(is, false);


            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // JPG图片没有Aphla通道使用该颜色模式更加节省内存
            options.inPreferredConfig = Bitmap.Config.RGB_565;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private  void load(ImageView view){
        // 设置显示区域的矩形大小
        rect = new Rect((imageWidth-screenWidth)/ 2  , (imageHeight-screenHeigth)/ 2 , (imageWidth+screenWidth)/ 2, (imageHeight+screenHeigth) / 2);
        // 通过BitmapRegionDecoder来解析显示区域的图像
        Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, options);
        view.setImageBitmap(bitmap);
    }
    private  void move(ImageView view,int dx,int dy){
        if (rect.left+dx>0&&rect.top+dy>0&&rect.right+dx<imageWidth&&rect.bottom+dy<imageHeight)
            rect.offset(dx,dy);
        Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, options);
        view.setImageBitmap(bitmap);
    }
    private  void zoom(ImageView view,float scale){
        Log.d(TAG, "zoom: scale------------"+scale);
        int width = (int) (rect.width()*scale);
        width = width>screenWidth?width:screenWidth;
        int height = (int) (rect.height()*scale);
        height = height>screenHeigth?height:screenHeigth;
        int centerX = rect.centerX();
        int centerY = rect.centerY();
        if (centerX-width/2>0
                &&centerX+width/2<imageWidth
                &&centerY-height/2>0
                &&centerY+height/2<imageHeight
//                &&width>screenWidth
//                &&height>screenHeigth
                )
        {
//             width = (int) (width*multiple);
//             height = (int) (height*multiple);
            Log.d(TAG, "zoom:  width---------"+width);
            Log.d(TAG, "zoom:   height-------"+height);
            Log.d(TAG, "zoom: centerX--------"+centerX);
            Log.d(TAG, "zoom:  centerY-------"+centerY);
            rect = new Rect(centerX-width/2  , centerY-height/2 , centerX+width/2, centerY+height/2);
        }
        Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, options);
        view.setImageBitmap(bitmap);
    }




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

//            FrameLayout layout = (FrameLayout) view.getParent();
            view.setOnTouchListener(new TouchListener());

        }
        void  translateAnimation(View view, float desX, float desY){
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
                        if (mode==MODE_CLICK){
                            mode = MODE_DRAG;
                        }
                        // 拖拉图片
                        if (mode == MODE_DRAG) {
                            Log.d(TAG, "onTouch:          move");
                            int dx = (int)(event.getX() - startPoint.x); // 得到x轴的移动距离
                            int dy = (int)(event.getY() - startPoint.y); // 得到x轴的移动距离
                            // 在没有移动之前的位置上进行移动
                            move(imageView,-dx,-dy);
//                            matrix.set(currentMatrix);
//                            matrix.postTranslate(dx, dy);
                        }
                        // 放大缩小图片
                        else if (mode == MODE_ZOOM) {
                            float endDis = distance(event);// 结束距离
                            if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                                float scale = endDis / startDis;// 得到缩放倍数
                                zoom(imageView,1/scale);
//                                matrix.set(currentMatrix);
//                                matrix.postScale(scale, scale,midPoint.x,midPoint.y);
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




}
