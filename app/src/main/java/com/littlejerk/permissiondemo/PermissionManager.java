package com.littlejerk.permissiondemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

/**
 * @author : HHotHeart
 * @date : 2021/8/21 00:59
 * @desc : 权限申请弹框管理类
 */
public class PermissionManager {

    private static final String TAG = "PermissionManager";

    private PermissionManager() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }


    /**
     * 获取清单文件声明的权限列表
     */
    private static final List<String> PERMISSIONS = PermissionUtils.getPermissions();

    /**
     * 自定义权限组名
     */
    public static final String SMS = "MDroidS.permission-group.SMS";
    public static final String CONTACTS = "MDroidS.permission-group.CONTACTS";
    public static final String LOCATION = "MDroidS.permission-group.LOCATION";
    public static final String MICROPHONE = "MDroidS.permission-group.MICROPHONE";
    public static final String PHONE = "MDroidS.permission-group.PHONE";
    public static final String CAMERA = "MDroidS.permission-group.CAMERA";
    public static final String STORAGE = "MDroidS.permission-group.STORAGE";
    public static final String ALBUM = "MDroidS.permission-group.ALBUM";

    /**
     * 自定义权限组的归类，用户可自由扩展需要申请的权限
     */
    private static final String[] GROUP_CAMERA = new String[]{"android.permission.CAMERA"};
    private static final String[] GROUP_SMS = new String[]{"android.permission.SEND_SMS"};
    private static final String[] GROUP_CONTACTS = new String[]{"android.permission.READ_CONTACTS"};
    private static final String[] GROUP_LOCATION = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};
    private static final String[] GROUP_MICROPHONE = new String[]{"android.permission.RECORD_AUDIO"};
    private static final String[] GROUP_PHONE = new String[]{"android.permission.CALL_PHONE"};
    private static final String[] GROUP_STORAGE = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private static final String[] GROUP_PERMISSION = new String[]{SMS, CONTACTS, LOCATION, MICROPHONE, PHONE, ALBUM, CAMERA};

    /**
     * 权限申请弹框提示信息
     *
     * @param activity
     * @param callback
     * @param permissions {@link android.Manifest.permission#CALL_PHONE} 和 {@link PermissionManager#SMS}
     * @return
     */
    public static void permission(Activity activity,
                                  OnPermissionGrantCallback callback,
                                  @PermissionType String... permissions) {

        Map<String, String> map = PermissionManager.getPermissionDesMap(permissions);

        permission(activity, map, callback, permissions);
    }

    /**
     * @param activity    上下文
     * @param map         null:表示不弹权限申请用处信息框或无效权限
     *                    key:权限名，可以是单个权限或权限组名 value:申请该权限或权限组的用处信息
     * @param callback    权限回调
     * @param permissions 自定义权限组名
     */
    public static void permission(Activity activity,
                                  Map<String, String> map,
                                  final OnPermissionGrantCallback callback,
                                  @PermissionType String... permissions) {
        //表示不弹权限申请用处信息框或无效权限
        if (null == map) {
            permissionNoExplain(activity, callback, permissions);
            return;
        }

        if (activity == null) {
            Log.e(TAG, " activity == null");
            callback.onDenied();
            return;
        }
        //需要申请的权限（单个权限或权限组）
        Set<String> deniedPermission = map.keySet();
        if (ObjectUtils.isEmpty(deniedPermission)) {
            callback.onGranted();
            return;
        }
        Log.e(TAG, " deniedPermission：" + deniedPermission);

        StringBuilder sb = new StringBuilder();
        int i = 0;
        Set<String> manifestPermission = new LinkedHashSet<String>();

        for (String groupType : deniedPermission) {
            i++;
            String msg = map.get(groupType);
            sb.append(i).append("、").append(msg).append("\n");
            //权限组包含的所有权限
            String[] arrayPermission = PermissionManager.getPermissions(groupType);
            Collections.addAll(manifestPermission, arrayPermission);
        }
        String remindMsg = null;
        String sbStr = sb.toString();

        int endIndex = sbStr.lastIndexOf("\n");
        if (deniedPermission.size() == 1) {
            remindMsg = sbStr.substring(2, endIndex);
        } else {
            remindMsg = sbStr.substring(0, endIndex);
        }
        //权限组所包含的权限
        String[] permissionArray = manifestPermission.toArray(new String[manifestPermission.size()]);
        permissionExplain(activity, remindMsg, callback, permissionArray, permissions);

    }


    /**
     * 权限申请弹框解释申请理由或权限用处
     *
     * @param activity        上下文
     * @param remindMsg       权限申请理由或用处信息
     * @param callback        权限回调
     * @param permissionArray 权限组包含的权限，如{@link #GROUP_SMS}
     * @param permissions     自定义的权限组名,如{@link #SMS}
     */
    private static void permissionExplain(final Activity activity, String remindMsg,
                                          final OnPermissionGrantCallback callback,
                                          final String[] permissionArray,
                                          @PermissionType String... permissions) {
        //权限申请未通过，弹框提示
        AppDialogManager.getInstance().showPermissionRemindDialog(activity, remindMsg,
                (dialogInterface, i) -> callback.onDenied(),
                (dialogInterface, i) -> {
                    PermissionUtils.permission(permissionArray).callback(new PermissionUtils.FullCallback() {
                        @Override
                        public void onGranted(@NonNull List<String> permissionsGranted) {
                            callback.onGranted();
                        }

                        @Override
                        public void onDenied(@NonNull List<String> permissionsDeniedForever, @NonNull List<String> permissionsDenied) {
                            if (!ObjectUtils.isEmpty(permissionsDeniedForever)) {
                                //权限不再提示情况处理
                                permissionsDeniedForever(activity, callback, permissionsDeniedForever, permissions);
                            } else {
                                callback.onDenied();
                            }
                        }
                    }).request();
                });
    }

    /**
     * 处理权限不再提示的逻辑
     *
     * @param activity                 上下文
     * @param callback                 权限回调
     * @param permissionsDeniedForever 权限组不再提示的某些权限
     * @param permissions              自定义的权限组名,如{@link #SMS}
     */
    private static void permissionsDeniedForever(Activity activity,
                                                 OnPermissionGrantCallback callback,
                                                 List<String> permissionsDeniedForever,
                                                 @PermissionType String... permissions) {
        //根据返回的不再提示权限获取对应的自定义权限组
        List<String> requestPermissionList = getMatchPermissionGroup(permissionsDeniedForever, permissions);
        if (ObjectUtils.isEmpty(requestPermissionList)) {
            callback.onDenied();
            return;
        }

        //拼接设置权限提示语
        StringBuilder permissionRequestMsg = new StringBuilder();
        permissionRequestMsg.append(StringUtils.getString(R.string.dialog_permission_remind_prefix));
        for (String permission : requestPermissionList) {
            String permissionName = getPermissionName(permission);
            if (!StringUtils.isEmpty(permissionName))
                permissionRequestMsg.append(permissionName).append("、");
        }
        if (permissionRequestMsg.lastIndexOf("、") > 0) {
            permissionRequestMsg.deleteCharAt(permissionRequestMsg.lastIndexOf("、"));
        } else {
            callback.onDenied();
            return;
        }
        permissionRequestMsg.append(StringUtils.getString(R.string.dialog_permission_remind_suffix));
        AppDialogManager.getInstance().showPermissionSettingRemind(activity, permissionRequestMsg.toString(),
                (dialogInterface, i) -> callback.onDenied(),
                (dialogInterface, i) -> openAppSystemSettings());
    }

    /**
     * 获取对应权限列表
     *
     * @param rawPermissionList 权限名集合
     * @param permissions       自定义权限组名
     * @return
     */
    private static List<String> getMatchPermissionGroup(List<String> rawPermissionList, @PermissionType String... permissions) {
        if (ObjectUtils.isEmpty(rawPermissionList)) return null;
        List<String> permissionList = new LinkedList<>();
        if (!ObjectUtils.isEmpty(permissions)) {
            for (String permission : permissions) {
                for (String permissionRaw : getPermissions(permission)) {
                    if (rawPermissionList.contains(permissionRaw)) {
                        permissionList.add(permission);
                        break;
                    }
                }
            }
        } else {
            for (String permission : GROUP_PERMISSION) {
                for (String permissionRaw : getPermissions(permission)) {
                    if (rawPermissionList.contains(permissionRaw)) {
                        permissionList.add(permission);
                        break;
                    }
                }
            }
        }
        return permissionList;
    }

    /**
     * 权限申请不弹理由解释框
     *
     * @param activity    上下文
     * @param callback    权限申请回调
     * @param permissions 自定义权限组名
     */
    @SuppressLint("WrongConstant")
    private static void permissionNoExplain(final Activity activity,
                                            final OnPermissionGrantCallback callback,
                                            @PermissionType final String... permissions) {

        Set<String> manifestPermission = new LinkedHashSet<String>();
        for (String groupType : permissions) {
            //权限组包含的所有权限
            String[] arrayPermission = PermissionManager.getPermissions(groupType);
            Collections.addAll(manifestPermission, arrayPermission);
        }
        String[] permissionArray = manifestPermission.toArray(new String[manifestPermission.size()]);
        PermissionUtils.permission(permissionArray).callback(new PermissionUtils.FullCallback() {

            @Override
            public void onGranted(@NonNull List<String> permissionsGranted) {
                callback.onGranted();
            }

            @Override
            public void onDenied(@NonNull List<String> permissionsDeniedForever, @NonNull List<String> permissionsDenied) {
                if (!ObjectUtils.isEmpty(permissionsDeniedForever)) {
                    if (activity == null) {
                        callback.onDenied();
                        return;
                    }
                    permissionsDeniedForever(activity, callback, permissionsDeniedForever, permissions);

                } else {
                    callback.onDenied();
                }


            }
        }).request();
    }

    /**
     * 获取权限组
     *
     * @param permission 自定义权限组名
     * @return
     */
    private static String[] getPermissions(String permission) {

        switch (permission) {
            case CONTACTS:
                return GROUP_CONTACTS;
            case SMS:
                return GROUP_SMS;
            case STORAGE:
            case ALBUM:
                return GROUP_STORAGE;
            case LOCATION:
                return GROUP_LOCATION;
            case PHONE:
                return GROUP_PHONE;
            case MICROPHONE:
                return GROUP_MICROPHONE;
            case CAMERA:
                return GROUP_CAMERA;
            default:
                return new String[]{permission};
        }

    }


    /**
     * 获取请求权限中需要申请的权限和其相应用处信息
     *
     * @param permissions
     * @return key:权限名，可以是单个权限或权限组名 value:申请该权限或权限组的用处信息
     */
    private static Map<String, String> getPermissionDesMap(@NonNull String... permissions) {

        Log.e(TAG, " permissions.length：" + permissions.length);
        //无效权限组
        if (ObjectUtils.isEmpty(permissions)) {
            Log.e(TAG, "无效权限,请确认权限是否正确或是否已在清单文件声明");
            return null;
        }

        Set<String> deniedPermissions = PermissionManager.getDeniedPermissionGroup(permissions);
        //无效权限组，即不在清单文件中
        if (null == deniedPermissions) {
            Log.e(TAG, "无效权限,请确认权限是否正确或是否已在清单文件声明");
            return null;
        }

        Map<String, String> map = new HashMap<String, String>();
        for (String permission : deniedPermissions) {
            String remindMsg = PermissionManager.getPermissionDes(permission);
            map.put(permission, remindMsg);
        }

        return map;

    }

    /**
     * 获取未通过的权限列表
     *
     * @param permissions
     * @return
     */
    private static Set<String> getDeniedPermissionGroup(String... permissions) {
        //无效权限（不在清单文件中）
        boolean isInvalid = true;
        LinkedHashSet<String> deniedPermissions = new LinkedHashSet<String>();

        for (int length = permissions.length, i = 0; i < length; ++i) {
            String permission = permissions[i];
            //权限组或单个权限
            String[] groupPermission = PermissionManager.getPermissions(permission);
            for (String aPermission : groupPermission) {
                //检查清单文件声明权限
                if (!PERMISSIONS.contains(aPermission)) {
                    Log.e(TAG, "无效权限,请确认权限是否正确或是否已在清单文件声明：" + aPermission);
                    continue;
                }
                isInvalid = false;
                //权限未通过
                if (!PermissionUtils.isGranted(aPermission)) {
                    Log.e(TAG, " denied：" + permission);
                    deniedPermissions.add(permission);
                    break;
                }
                Log.e(TAG, " granted：" + permission);
            }
        }
        //无效权限（不在清单文件中）
        if (isInvalid) {
            return null;
        }

        return deniedPermissions;
    }

    /**
     * 获取请求权限相应的提示信息
     *
     * @param permission
     * @return
     */
    private static String getPermissionDes(String permission) {

        String remindMsg = null;
        switch (permission) {
            case STORAGE:
                remindMsg = StringUtils.getString(R.string.permission_storage_des);
                break;
            case ALBUM:
                remindMsg = StringUtils.getString(R.string.permission_photo_des);
                break;
            case CAMERA:
                remindMsg = StringUtils.getString(R.string.permission_camera_des);
                break;
            case LOCATION:
                remindMsg = StringUtils.getString(R.string.permission_location_des);
                break;
            case MICROPHONE:
                remindMsg = StringUtils.getString(R.string.permission_microphone_des);
                break;
            case CONTACTS:
                remindMsg = StringUtils.getString(R.string.permission_contacts_des);
                break;
            case PHONE:
                remindMsg = StringUtils.getString(R.string.permission_phone_des);
                break;
            case SMS:
                remindMsg = StringUtils.getString(R.string.permission_sms_des);
                break;
            default:
                //单个权限申请扩展
                break;
        }

        return remindMsg;
    }

    /**
     * 获取请求权限名
     *
     * @param permission 自定义权限组名
     * @return
     */
    private static String getPermissionName(String permission) {

        String name = null;
        switch (permission) {
            case STORAGE:
                name = "存储";
                break;
            case ALBUM:
                name = "相册";
                break;
            case CAMERA:
                name = "相机";
                break;
            case LOCATION:
                name = "定位";
                break;
            case MICROPHONE:
                name = "麦克风";
                break;
            case CONTACTS:
                name = "通讯录";
                break;
            case PHONE:
                name = "电话";
                break;
            case SMS:
                name = "短信";
                break;
            default:
                //单个权限申请扩展
                break;
        }

        return name;
    }

    /**
     * 判断权限是否已申请过
     * 特别说明：
     * 使用PermissionUtils三方框架权限申请时，如果要调用PermissionUtils.isGranted(permissionGroup)
     * 要确保申请的权限或权限组已经在清单文件声明过，不然会恒为false
     *
     * @param permissions
     * @return
     */
    public static boolean isGranted(String... permissions) {
        if (ObjectUtils.isEmpty(permissions)) {
            return false;
        }
        Set<String> deniedPermissions = PermissionManager.getDeniedPermissionGroup(permissions);
        if (null == deniedPermissions) return false;

        return ObjectUtils.isEmpty(deniedPermissions);
    }

    /**
     * 打开应用详情页
     */
    private static void openAppSystemSettings() {
        PermissionUtils.launchAppDetailsSettings();
    }


    /**
     * 自定义权限组类型
     */
    @StringDef({SMS, MICROPHONE, STORAGE, ALBUM, CONTACTS, PHONE, LOCATION, CAMERA})
    @Retention(RetentionPolicy.SOURCE)
    private @interface PermissionType {
    }

    /**
     * 权限申请回调
     */
    public interface OnPermissionGrantCallback {
        void onGranted();

        void onDenied();
    }

}