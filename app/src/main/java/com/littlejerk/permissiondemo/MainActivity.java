package com.littlejerk.permissiondemo;

import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //可申请多个权限组（自定义）
                PermissionManager.permission(MainActivity.this, new PermissionManager.OnPermissionGrantCallback() {
                    @Override
                    public void onGranted() {
                        ToastUtils.showShort("onGranted");
                    }

                    @Override
                    public void onDenied() {
                        ToastUtils.showShort("onDenied");
                    }
                }, PermissionManager.CAMERA, PermissionManager.ALBUM);
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //可自定义申请理由
                Map<String, String> map = new HashMap<>();
                map.put(PermissionManager.CAMERA, "申请CAMERA理由");
                map.put(PermissionManager.ALBUM, "申请ALBUM理由");
                PermissionManager.permission(MainActivity.this, map, new PermissionManager.OnPermissionGrantCallback() {
                    @Override
                    public void onGranted() {
                        ToastUtils.showShort("onGranted");
                    }

                    @Override
                    public void onDenied() {
                        ToastUtils.showShort("onDenied");
                    }
                }, PermissionManager.CAMERA, PermissionManager.ALBUM);
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //不需要申请理由弹框
                PermissionManager.permission(MainActivity.this, null, new PermissionManager.OnPermissionGrantCallback() {
                    @Override
                    public void onGranted() {
                        ToastUtils.showShort("onGranted");
                    }

                    @Override
                    public void onDenied() {
                        ToastUtils.showShort("onDenied");
                    }
                }, PermissionManager.CAMERA, PermissionManager.ALBUM);
            }
        });
    }
}