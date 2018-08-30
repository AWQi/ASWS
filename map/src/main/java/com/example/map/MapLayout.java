package com.example.map;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MapLayout  extends FrameLayout {
    private static final String TAG = "MapActivity";
    private ImageView map = null;
    private Button vernier = null;
    private List<Part> partList = null;
    private  Context context;
    public MapLayout(@NonNull Context context) {
        super(context);
        this.context = context;
//        map = findViewById(R.id.map);
        map = new ImageView(context);
        this.addView(map);
        vernier = new Button(context);
        this.addView(vernier);
//        vernier = findViewById(R.id.vernier);
        map.setOnTouchListener(new TouchListener());
        partList =  analysisMapXml();
    }

    public MapLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.context = context;
//        map = findViewById(R.id.map);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        map = new ImageView(context);
        map.setLayoutParams(layoutParams);
        map.setScaleType(ImageView.ScaleType.MATRIX);
        this.addView(map);
        map.setImageResource(R.drawable.map);
        LayoutParams btnParam = new LayoutParams(10, 10);
        vernier = new Button(context);
        vernier.setLayoutParams(btnParam);
        this.addView(vernier);
//        vernier = findViewById(R.id.vernier);
        map.setOnTouchListener(new TouchListener());
        partList =  analysisMapXml();
    }

    void translateAnimation(View view, float desX, float desY) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, "translationX", view.getX(), desX);
        animatorX.setDuration(500);
        animatorX.start();
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(view, "translationY", view.getY(), desY);
        animatorY.setDuration(500);
        animatorY.start();

    }

    private class TouchListener implements View.OnTouchListener {

        /**
         * 记录是拖拉照片模式还是放大缩小照片模式
         */
        private int mode = 0;// 初始状态
        private static final int MODE_DEFAULT = 0;
        /**
         * 拖拉照片模式
         */
        private static final int MODE_DRAG = 1;
        /**
         * 放大缩小照片模式
         */
        private static final int MODE_ZOOM = 2;
        private static final int MODE_CLICK = 3;
        private static final int MIN_DIS = 50;

        /**
         * 用于记录开始时候的坐标位置
         */
        private PointF startPoint = new PointF();
        /**
         * 用于记录拖拉图片移动的坐标位置
         */
        private Matrix matrix = new Matrix();
        /**
         * 用于记录图片要进行拖拉时候的坐标位置
         */
        private Matrix currentMatrix = new Matrix();

        /**
         * 两个手指的开始距离
         */
        private float startDis;
        /**
         * 两个手指的中间点
         */
        private PointF midPoint;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 手指压下屏幕
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_CLICK;
                    // 记录ImageView当前的移动位置
                    currentMatrix.set(map.getImageMatrix());
                    startPoint.set(event.getX(), event.getY());
                    break;
                // 手指在屏幕上移动，改事件会被不断触发
                case MotionEvent.ACTION_MOVE:
                    if (mode == MODE_CLICK) {
                        mode = MODE_DRAG;
                    }
                    // 拖拉图片
                    if (mode == MODE_DRAG) {
                        float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                        float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                        Log.d(TAG, "dx:   ----------    "+dx);
                        Log.d(TAG, "dy: -----------      "+dy);
                        // 在没有移动之前的位置上进行移动
                        matrix.set(currentMatrix);
                        float[] s = new float[9];
                        matrix.getValues(s);
                        float transX = s[Matrix.MTRANS_X];
                        float transY = s[Matrix.MTRANS_Y];
                        Log.d(TAG, "x:----------------------: "+transX);
                        Log.d(TAG, "y:----------------------: "+transY);

//                       if (v.getLeft()-x)
//                        dx = v.getLeft()+dx
                        matrix.postTranslate(dx, dy);
                    }
                    // 放大缩小图片
                    else if (mode == MODE_ZOOM) {
                        float endDis = distance(event);// 结束距离
                        if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                            float scale = endDis / startDis;// 得到缩放倍数
                            matrix.set(currentMatrix);
                            matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        }
                    }
                    break;
                // 手指离开屏幕
                case MotionEvent.ACTION_UP:
                    float x = event.getX();
                    float y = event.getY();
                    Log.d(TAG, "onTouch: x------" + x);
                    Log.d(TAG, "onTouch: y------" + y);
                    translateAnimation(vernier, x, y);
                    String partName = matchArea(partList,x,y);
                    Toast.makeText(context,partName,Toast.LENGTH_SHORT).show();
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
                        currentMatrix.set(map.getImageMatrix());
                    }
                    break;
            }
            map.setImageMatrix(matrix);
            return true;
        }

        /**
         * 计算两个手指间的距离
         */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        /**
         * 计算两个手指间的中间点
         */
        private PointF mid(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }

    }


    //  解析XML
    public List<Part> analysisMapXml() {
        //获取网络XML数据
        try {
            AssetManager assetManager = context.getAssets();

            InputStream is = assetManager.open("map.xml");
            //解析XMLDOM解析=====================================
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(is);
            //获取根标签
            Element element = document.getDocumentElement();
            Log.i("test", "根标签：" + element.getNodeName());
            NodeList nodeList = element.getElementsByTagName("part");
            List<Part> partList = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                //获取单个
                Element personElement = (Element) nodeList.item(i);
                //获取<person>属性id的值
                int id  =  Integer.parseInt(personElement.getAttribute("id"));
                //获取<person>下面的子标签<name><age>的值
                Element nameElement = (Element) personElement.getElementsByTagName("name").item(0);
                String name = nameElement.getTextContent();


                Element lElement = (Element) personElement.getElementsByTagName("l").item(0);
                float l = Float.parseFloat(lElement.getTextContent());
                Element tElement = (Element) personElement.getElementsByTagName("t").item(0);
                float t = Float.parseFloat(tElement.getTextContent());
                Element rElement = (Element) personElement.getElementsByTagName("r").item(0);
                float r = Float.parseFloat(rElement.getTextContent());
                Element bElement = (Element) personElement.getElementsByTagName("b").item(0);
                float b = Float.parseFloat(bElement.getTextContent());
                Part part = new Part(id,name,l,t,r,b);
                Log.d(TAG, "analysisMapXml:------------ "+part.toString());
                partList.add(part);
            }
            return  partList;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }
    public  String matchArea(List<Part> partList,float x,float y){
        for (Part p :partList){
            if (x>p.getL()&&x<p.getR()&&y>p.getT()&&y<p.getB()){
                return p.getName();
            }
        }
        return null;
    }
}
