package com.aiming.mdt.sdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "MainActivity";
    //广告初始化表示 0 进行中 1 成功 2 失败
    public static int adInitFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        findViewById(R.id.btn_native).setOnClickListener(this);
        findViewById(R.id.btn_interstitial1).setOnClickListener(this);
        findViewById(R.id.btn_interstitial2).setOnClickListener(this);
        findViewById(R.id.btn_video1).setOnClickListener(this);
        findViewById(R.id.btn_video2).setOnClickListener(this);
        findViewById(R.id.btn_interactive).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (checkInitStatus()) {
            return;
        }
        int id = v.getId();
        Intent intent = new Intent();
        switch (id) {
            case R.id.btn_native:
                break;
            case R.id.btn_interstitial1:
                break;
            case R.id.btn_interstitial2:
                break;
            case R.id.btn_video1:
                break;
            case R.id.btn_video2:
                break;
            case R.id.btn_interactive:
                break;
        }
    }

    private boolean checkInitStatus() {
        if (adInitFlag == 0) {
            Toast.makeText(this, "广告初始化进行中，请等待---", Toast.LENGTH_SHORT).show();
        } else if (adInitFlag == 1) {
            return false;
        } else if (adInitFlag == 2) {
            Toast.makeText(this, "广告初始化失败", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
