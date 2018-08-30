package com.example.qi.ndktest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

public class FlowChartActivity extends AppCompatActivity {
    private static final String TAG = "FlowChartActivity";
private ProgressBar progressBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_chart);
        progressBar = findViewById(R.id.progressBar);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while(i<100) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                progressBar.setProgress(i);
                i+=10;
                Log.d(TAG, "run: i="+i);
            }
        }).start();


    }
}
