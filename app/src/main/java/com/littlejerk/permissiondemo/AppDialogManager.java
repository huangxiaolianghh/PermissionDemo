package com.littlejerk.permissiondemo;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.StringUtils;
import com.flyco.roundview.RoundTextView;

/**
 * @author : HHotHeart
 * @date : 2021/8/21 01:03
 * @desc : Dialog公共管理类
 */
public class AppDialogManager {

    private AppDialogManager() {
    }

    public static AppDialogManager getInstance() {
        return Holder.instance;
    }

    private static class Holder {
        private static AppDialogManager instance = new AppDialogManager();
    }

    /**
     * 普通弹框-只有一个实例的弹框
     */
    private BaseDialog mDialog = null;

    /**
     * 扩展的Dialog回调
     */
    public interface DialogClickCallback {
        void onClick(int index);
    }

    /**
     * 权限申请弹框
     *
     * @param activity
     * @param content
     * @param positiveLister
     */
    public void showPermissionRemindDialog(Activity activity, String content,
                                           DialogInterface.OnClickListener negativeLister, DialogInterface.OnClickListener positiveLister) {
        showCommonDialog(activity,
                activity.getString(R.string.dialog_permission_title), content,
                activity.getString(R.string.dialog_permission_refuse), negativeLister,
                activity.getString(R.string.dialog_permission_allow), positiveLister);
    }

    /**
     * 权限设置弹框提示
     *
     * @param activity
     * @param content
     * @param positiveLister
     */
    public void showPermissionSettingRemind(Activity activity, String content,
                                            DialogInterface.OnClickListener negativeLister, DialogInterface.OnClickListener positiveLister) {
        showCommonDialog(activity,
                activity.getString(R.string.dialog_permission_title), content,
                "取消", negativeLister,
                "设置", positiveLister);
    }

    /**
     * 有Positive按钮的Dialog
     *
     * @param activity
     * @param title
     * @param content
     * @param positive
     * @param positiveLister
     */
    public void showPositiveDialog(Activity activity, String title, String content,
                                   String positive, DialogInterface.OnClickListener positiveLister) {
        showCommonDialog(activity, title, content, null, null, positive, positiveLister);
    }

    /**
     * 有确认和取消按钮（没有title）
     *
     * @param activity
     * @param content
     * @param positiveLister
     */
    public void showCommonDialog(Activity activity, String content, DialogInterface.OnClickListener positiveLister) {
        showCommonDialog(activity, null, content,
                activity.getString(R.string.dialog_btn_cancel), null,
                activity.getString(R.string.dialog_btn_confirm), positiveLister);
    }

    /**
     * 公共方法
     *
     * @param activity
     * @param title
     * @param content
     * @param negative
     * @param negativeLister
     * @param positive
     * @param positiveLister
     */
    public void showCommonDialog(Activity activity, String title, String content,
                                 String negative, DialogInterface.OnClickListener negativeLister,
                                 String positive, DialogInterface.OnClickListener positiveLister) {
        showCommonDialog(activity, title, content, negative, negativeLister, positive, positiveLister, true);
    }

    /**
     * 公共方法
     *
     * @param activity
     * @param title
     * @param content
     * @param negative
     * @param negativeLister
     * @param positive
     * @param positiveLister
     * @param isClickDismiss
     */
    public void showCommonDialog(Activity activity, String title, String content,
                                 String negative, DialogInterface.OnClickListener negativeLister,
                                 String positive, DialogInterface.OnClickListener positiveLister, boolean isClickDismiss) {
        showDialog(activity, title, content, negative, negativeLister, positive, positiveLister,
                false, false, null,
                true, isClickDismiss);
    }


    /**
     * Dialog基本方法
     *
     * @param activity                 显示Dialog的Activity
     * @param title                    Dialog 标题
     * @param content                  Dialog 内容
     * @param negative                 左边按钮
     * @param negativeLister           左边按钮的点击事件
     * @param positive                 右边按钮
     * @param positiveLister           右边按钮的点击事件
     * @param isCanceledOnTouchOutside 点击非Dialog内容部分是否允许Dismiss
     * @param isCancelable             点击后退键是否允许Dismiss
     * @param dismissListener          Dialog消失的监听事件
     * @param isMultiDialog            是否允许多个Dialog同时存在
     * @param isClickDismiss           点击按钮是否允许dismiss Dialog
     */
    private void showDialog(Activity activity,
                            String title,
                            String content,
                            String negative, final DialogInterface.OnClickListener negativeLister,
                            String positive, final DialogInterface.OnClickListener positiveLister,
                            boolean isCanceledOnTouchOutside,
                            boolean isCancelable,
                            final DialogInterface.OnDismissListener dismissListener,
                            boolean isMultiDialog,
                            final boolean isClickDismiss) {

        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_common_view, null);
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        TextView tvContent = view.findViewById(R.id.tv_dialog_content);
        tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        RoundTextView rtvLeft = view.findViewById(R.id.rtv_left);
        RoundTextView rtvRight = view.findViewById(R.id.rtv_right);
        //显示title逻辑
        if (!StringUtils.isEmpty(title)) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setVisibility(View.GONE);
        }
        //内容为空时，直接return,不显示dialog
        if (StringUtils.isEmpty(content)) {
            return;
        }
        //显示content逻辑
        tvContent.setText(content);
        tvContent.setVisibility(View.VISIBLE);
        //没有标题时,content的上下距离30dp、文字大小14sp
        if (StringUtils.isEmpty(title)) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvContent.getLayoutParams();
            params.setMargins(
                    SizeUtils.dp2px(15), SizeUtils.dp2px(30),
                    SizeUtils.dp2px(15), SizeUtils.dp2px(30));
            tvContent.setLayoutParams(params);
        }

        //沒有title,只有右边按钮时，content文字大小为12sp
        if (StringUtils.isEmpty(title) && StringUtils.isEmpty(negative) && !StringUtils.isEmpty(positive)) {
            tvContent.setTextSize(12);
        }

        if (!StringUtils.isEmpty(negative)) {
            rtvLeft.setText(negative);
            rtvLeft.setVisibility(View.VISIBLE);
        } else {
            rtvLeft.setVisibility(View.GONE);
        }
        if (!StringUtils.isEmpty(positive)) {
            rtvRight.setText(positive);
            rtvRight.setVisibility(View.VISIBLE);
        } else {
            rtvRight.setVisibility(View.GONE);
        }

        if (isMultiDialog) {
            //允许多个Dialog存在
            final BaseDialog dialog = new BaseDialog(activity);
            //外部是否可以取消
            dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
            //返回是否可以取消
            dialog.setCancelable(isCancelable);

            dialog.setOnDismissListener(dialog1 -> {
                if (dismissListener != null) dismissListener.onDismiss(dialog1);
            });

            rtvLeft.setOnClickListener(v -> {
                if (isClickDismiss) {
                    dialog.dismiss();
                }
                if (negativeLister != null) {
                    negativeLister.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                }

            });
            rtvRight.setOnClickListener(v -> {
                if (isClickDismiss) {
                    dialog.dismiss();
                }
                if (positiveLister != null) {
                    positiveLister.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                }

            });
            dialog.show(view);
        } else {
            //只允许一个Dialog存在
            resetLatestDialog(mDialog);
            mDialog = new BaseDialog(activity);
            //外部是否可以取消
            mDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
            //返回是否可以取消
            mDialog.setCancelable(isCancelable);

            mDialog.setOnDismissListener(dialog -> {
                if (dismissListener != null) dismissListener.onDismiss(dialog);
            });

            rtvLeft.setOnClickListener(v -> {
                if (isClickDismiss) {
                    mDialog.dismiss();
                }
                if (negativeLister != null) {
                    negativeLister.onClick(mDialog, DialogInterface.BUTTON_NEGATIVE);
                }

            });
            rtvRight.setOnClickListener(v -> {
                if (isClickDismiss) {
                    mDialog.dismiss();
                }
                if (positiveLister != null) {
                    positiveLister.onClick(mDialog, DialogInterface.BUTTON_POSITIVE);
                }

            });
            mDialog.show(view);
        }

    }

    /**
     * 释放掉最近显示的Dialog
     *
     * @param dialog
     */
    private void resetLatestDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

}

