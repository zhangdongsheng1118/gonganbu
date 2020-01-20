package com.scoopit.weedfs.client.outer;

import android.content.Context;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.scoopit.weedfs.client.AssignParams;
import com.scoopit.weedfs.client.Assignation;
import com.scoopit.weedfs.client.ReplicationStrategy;
import com.scoopit.weedfs.client.WeedFSClient;
import com.scoopit.weedfs.client.WeedFSClientBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UploadAndGetUrl {

    static final URL MASTER_URL;

    static {
        try {
//            MASTER_URL = new URL("http://123.56.100.207:9333");
//            MASTER_URL = new URL("http://114.255.88.232:9333");
            MASTER_URL = new URL("http://47.96.101.159:9333");
        } catch (MalformedURLException u) {
            throw new RuntimeException(u);
        }
    }

    public static interface CallBack {

        public void uploadFinish(String url);

        public void uploadProgress(FileInfo info);
    }

    public static class FileInfo {
        public int progress;
        public String url;
//        public int isUpload;
    }

    public static void uploadFile(Context context, String path, CallBack cb) {
        Observable.create(new Observable.OnSubscribe<FileInfo>() {
            @Override
            public void call(Subscriber<? super FileInfo> subscriber) {

                String ext = path.substring(path.lastIndexOf("."));
//                ext = ext.replace("voice","mp3");
                try {
                    String fileName = TimeUtils.getNowString(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())) + ext;
                    WeedFSClient client = WeedFSClientBuilder.createBuilder().setMasterUrl(MASTER_URL).build();
                    Assignation a = null;

                    a = client.assign(new AssignParams(null, 5, ReplicationStrategy.None));

                    File file = new File(path);
                    long totalSize = file.length();
                    String url = a.location.publicUrl + "/" + a.weedFSFile.fid + ext;
//                    int writtenSize = client.write(a.weedFSFile, a.location, new FileInputStream(file), fileName, new ProgressListener() {
                    int writtenSize = client.write(a.weedFSFile, a.location, file, fileName, new ProgressListener() {
                        private long lastTime = 0;

                        @Override
                        public void transferred(long transferedBytes, int progress) {
                            int progressData = (int) Math.floor(100 * transferedBytes / totalSize);

                            if (progress >= 100) {
                                LogUtils.w("upload and get url transferedBytes:" + transferedBytes + "    progress:" + progress + " url:" + url);

                                FileInfo file = new FileInfo();
                                file.progress = progress;
                                file.url = url;
                                subscriber.onNext(file);
                                return;
                            }

                            if (System.currentTimeMillis() - lastTime >= 500) {
                                lastTime = System.currentTimeMillis();
                                FileInfo file = new FileInfo();
                                file.progress = progressData;
                                subscriber.onNext(file);
                                LogUtils.w("upload and get url transferedBytes:" + transferedBytes + "    totalSize:" + totalSize);
                            }
                        }
                    });

//                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(null);
                }
            }
        }).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .sample(500, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Observer<FileInfo>() {
                    @Override
                    public void onNext(FileInfo url) {
                        com.yixin.tinode.util.LogUtils.i("upload progress ts" + url.progress);
                        if (StringUtils.isEmpty(url.url)) {
                            cb.uploadProgress(url);
                        } else {
                            cb.uploadFinish(url.url);
                        }
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        cb.uploadFinish(null);
                        LogUtils.e(e);
//                        Toast.makeText(context, "文件上传失败!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
