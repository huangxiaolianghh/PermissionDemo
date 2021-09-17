package com.littlejerk.permissiondemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.blankj.utilcode.util.ActivityUtils;

import androidx.annotation.NonNull;

/**
 * @author : HHotHeart
 * @date : 2021/9/17 12:46
 * @desc : 描述
 */
public class BaseDialog extends Dialog {
    private Context context;


    public BaseDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
        this.context = context;
    }


    /**
     * Dialog显示前处理逻辑
     *
     * @param view Dialog内容布局
     */
    public void show(View view) {
        Window window = getWindow();
        if (window == null) return;
        window.setContentView(view);
        WindowManager.LayoutParams pl = window.getAttributes();
        pl.gravity = Gravity.CENTER; //位置
        pl.height = WindowManager.LayoutParams.WRAP_CONTENT;
        int width = window.getDecorView().getResources().getDisplayMetrics().widthPixels;
        pl.width = (int) (width * 0.9);
        window.setAttributes(pl);
        show();
    }

    @Override
    public void show() {
        Activity activity = ActivityUtils.getActivityByContext(context);
        if (activity != null && !activity.isFinishing()) {
            super.show();
        }
    }

    @Override
    public void dismiss() {
        Activity activity = ActivityUtils.getActivityByContext(context);
        if (activity != null && !activity.isFinishing()) {
            super.dismiss();
        }
    }

    /**
     * 是否是外部区域
     *
     * @param context
     * @param event
     * @return
     */
    public boolean isOutOfBounds(Context context, MotionEvent event) {

        final int x = (int) event.getX();
        final int y = (int) event.getY();

        final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
        final Window window = getWindow();
        if (window == null) return false;
        final View decorView = getWindow().getDecorView();
        return (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop)) || (y > (decorView.getHeight() + slop));
    }
}