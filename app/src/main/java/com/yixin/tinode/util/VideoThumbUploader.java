package com.yixin.tinode.util;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 视频缩略图生成及上传
 */
public class VideoThumbUploader {
    private String path;

    static VideoThumbUploader instance;

    public static VideoThumbUploader getInstance() {
        if (instance == null) {
            synchronized (VideoThumbUploader.class) {
                if (instance == null) {
                    instance = new VideoThumbUploader();
                }
            }
        }
        return instance;
    }

    // @SuppressLint("NewApi")
    private VideoThumbUploader() {

    }

    public static interface Callback {

        public void createThumb(File img);
    }

    private Callback cb;

    /**
     * 同步保存图片
     *
     * @param path
     * @param width
     * @param height
     */
    public File createThumbSync(String path, int width, int height) {
        return saveBitmapFile(path, width, height);
    }


    public void createThumb(String path, int width, int height, Callback cb) {
        this.cb = cb;
        new MyBobAsynctack(path, width, height).execute(path);
    }

    class MyBobAsynctack extends AsyncTask<String, Void, File> {
        private String path;
        private int width;
        private int height;

        public MyBobAsynctack(String path, int width,
                              int height) {
            this.path = path;
            this.width = width;
            this.height = height;
        }

        @Override
        protected File doInBackground(String... params) {
            return saveBitmapFile(params[0], width, height);
        }

        @Override
        protected void onPostExecute(File bitmap) {
            if (cb != null) {
                cb.createThumb(bitmap);
            }
        }
    }

    private static File saveBitmapFile(String path, int width, int height) {
        File img = null;
        try {
            Bitmap bitmap = createVideoThumbnail(path, width, height,
                    MediaStore.Video.Thumbnails.MICRO_KIND);
            // 加入缓存中
            File video = new File(path);
            String name = video.getName().substring(0, video.getName().lastIndexOf("."));
            img = saveBitmapFile(bitmap, PathUtil.getInstance().getFilePathStr() + name + ".jpg");
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
        }

        return img;
    }

    /**
     * 把batmap 转file
     *
     * @param bitmap
     * @param filepath
     */
    private static File saveBitmapFile(Bitmap bitmap, String filepath) {
        File file = new File(filepath);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private static Bitmap createVideoThumbnail(String vidioPath, int width,
                                               int height, int kind) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(vidioPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
}
