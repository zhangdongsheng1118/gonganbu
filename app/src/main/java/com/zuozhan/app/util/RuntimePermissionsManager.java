package com.zuozhan.app.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class RuntimePermissionsManager {

    private static final String TAG = "RuntimePermissionsManager";
    public static final int PERMISSION_REQUEST_CODE = 1001;

    private static final String[] NECESSARY_PERMISSIONS = {
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_PHONE_STATE,
            //位置
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            //相机、麦克风
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            //存储空间
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private static final String[] OPTIONAL_PERMISSIONS = {
        //        Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static boolean hasNeedRequiredPermissions(
            Activity activity) { // Whether you need to apply for permission
        for (String permission : NECESSARY_PERMISSIONS) {
            if (isPermissionNoGranted(activity, permission)) {
                return true;
            }
        }

        for (String permission : OPTIONAL_PERMISSIONS) {
            if (isPermissionNoGranted(activity, permission)) {
                return true;
            }
        }

        return false;
    }

    private static List<String> getNoGrantedPermissions(Activity activity) {
        List<String> noGrantedPermissions = new ArrayList<String>();

        for (String permission : NECESSARY_PERMISSIONS) {
            if (isPermissionNoGranted(activity, permission)) {
                noGrantedPermissions.add(permission);
            }
        }

        for (String permission : OPTIONAL_PERMISSIONS) {
            if (isPermissionNoGranted(activity, permission)) {
                noGrantedPermissions.add(permission);
            }
        }

        return noGrantedPermissions;
    }

    private static boolean isPermissionNoGranted(Activity activity, String permission) {
        return activity.checkCallingOrSelfPermission(permission)
                != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean needRequestPermission(Activity activity, String permission) {
        if (activity == null) return false;

        boolean needRequest = false;
        if (Build.VERSION.SDK_INT >= 24) {
            needRequest = isPermissionNoGranted(activity, permission);
        }

        return needRequest;
    }

    private static boolean hasDeniedNecessaryPermission(String[] permissions, int[] grantResults) {
        if (permissions == null || permissions.length == 0) return false;
        if (grantResults == null || grantResults.length == 0) return false;

        for (int i = 0; i < grantResults.length; ++i) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                if (containedInNecessaryPermissions(permissions[i])) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean containedInNecessaryPermissions(String permission) {
        for (String eachPermission : NECESSARY_PERMISSIONS) {
            if (eachPermission.equals(permission)) {
                return true;
            }
        }

        return false;
    }

    public static boolean whetherAllPermissionsGranted(Activity activity) {
        if (activity == null) return false;

        boolean allPermissionsGranted = true;
        // Android N need dynamic permission checking
        if (Build.VERSION.SDK_INT >= 23) {
            if (hasNeedRequiredPermissions(activity)) {
                LogUtil.d(TAG, "need required permission.");
                allPermissionsGranted = false;
            }
        }
        return allPermissionsGranted;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestRequiredPermissions(Activity activity) {
        if (activity == null) return;

        List<String> requiredPermissions = getNoGrantedPermissions(activity);
        if (requiredPermissions.isEmpty()) {
            return;
        }

        String[] permissions = requiredPermissions.toArray(new String[requiredPermissions.size()]);
        activity.requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    public static boolean hasDeniedPermissions(String[] permissions, int[] grantResults) {
        return hasDeniedNecessaryPermission(permissions, grantResults);
    }
}
