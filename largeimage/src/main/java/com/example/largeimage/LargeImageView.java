package com.example.largeimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

public class LargeImageView extends View {
    private BitmapRegionDecoder mDecoder;
    /**
     * 图片的宽高
     */
    private int mImageWidth, mImageHeight;
    /**
     * 矩形显示的区域（volatile修饰的对象线程安全）
     */
    private volatile Rect mRect = new Rect();
    /**
     * 手势检测
     */
    private MoveGestureDetector mDetector;
    private static final BitmapFactory.Options options = new BitmapFactory.Options();

    static {
        options.inPreferredConfig = Bitmap.Config.RGB_565;
    }

    public LargeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setInputStream(InputStream is) {
        try {
            mDecoder = BitmapRegionDecoder.newInstance(is, false);
            BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
            tmpOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, tmpOptions);
            mImageWidth = tmpOptions.outWidth;
            mImageHeight = tmpOptions.outHeight;
            // Log.d("xns", "width:" + mImageWidth + " ,height:" + mImageHeight);
            // 重新布局View
            requestLayout();
            invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void init() {
        mDetector = new MoveGestureDetector(getContext(), new MoveGestureDetector.SimpleMoveGestureDetector() {
            @Override
            public boolean onMove(MoveGestureDetector detector) {
                int moveX = (int) detector.getMoveX();
                int moveY = (int) detector.getMoveY();
                // 只要图片在X或Y方向上大于View的宽高才进行矩形区域的偏移
                if (mImageWidth > getWidth()) {
                    mRect.offset(-moveX, 0);
                    checkWidth();
                    // 偏移后重新绘制显示图片
                    invalidate();
                }
                if (mImageHeight > getHeight()) {
                    mRect.offset(0, -moveY);
                    checkHeight();
                    invalidate();
                }
                return true;
            }
        });
    }

    private void checkHeight() {
        Rect rect = mRect;
        int imageHeight = mImageHeight;
        if (rect.bottom > imageHeight) {
            rect.bottom = imageHeight;
            rect.top = imageHeight - getHeight();
        }
        if (rect.top < 0) {
            rect.top = 0;
            rect.bottom = getHeight();
        }
    }

    private void checkWidth() {
        Rect rect = mRect;
        int imageWidth = mImageWidth;
        if (rect.right > imageWidth) {
            rect.right = imageWidth;
            rect.left = imageWidth - getWidth();
        }
        if (rect.left < 0) {
            rect.left = 0;
            rect.right = getWidth();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d("xns", "x:" + event.getX() + " ,y:" + event.getY());
        mDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int imageWidth = mImageWidth;
        int imageHeight = mImageHeight;
        // 默认显示图片中心区域
        mRect.left = imageWidth / 2 - width / 2;
        mRect.top = imageHeight / 2 - height / 2;
        mRect.right = mRect.left + width;
        mRect.bottom = mRect.top + height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = mDecoder.decodeRegion(mRect, options);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }
}



