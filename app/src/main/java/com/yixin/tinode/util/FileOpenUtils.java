package com.yixin.tinode.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.List;

/**
 * @创建者 CSDN_LQR
 * @描述 文件打开工具类
 */
public class FileOpenUtils {

    /**
     * 调用自带的视频播放器
     *
     * @param context
     * @param path
     */
    public static boolean openVideo(Context context, String path) {
        Uri uri = Uri.parse(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "video/mp4");// "video/mp4"
        context.startActivity(intent);
        return true;
    }

    /**
     * 调用自带的音频播放器
     *
     * @param context
     * @param path
     */
    private static void openAudio(Context context, String path) {
        File f = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(f), "audio/*");// "audio/mp3"
        context.startActivity(intent);
    }

    /**
     * 调用自带的图库
     *
     * @param context
     * @param path
     */
    private static void openPic(Context context, String path) {
        File f = new File(path);
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(f), "image/*");
        context.startActivity(intent);
    }

    /**
     * 调用手机上能打开对应类型文件的程序
     * <p>
     * <root-path/> 代表设备的根目录new File("/");
     * <files-path/> 代表context.getFilesDir()
     * <cache-path/> 代表context.getCacheDir()
     * <external-path/> 代表Environment.getExternalStorageDirectory()
     * <external-files-path>代表context.getExternalFilesDirs()
     * <external-cache-path>代表getExternalCacheDirs()
     *
     * @param context
     * @param path
     * @return true表示成功找到程序，false表示找不到能成功打开的程序
     */
    public static boolean openFile(Context context, String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        File file = new File(path);
        String mimeType = MimeTypeUtils.getMimeType(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(context, "com.yixin.tinode.fileProvider", file);

            List<ResolveInfo> resInfoList = context.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            intent.setDataAndType(Uri.fromFile(file), mimeType);
        }

        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
