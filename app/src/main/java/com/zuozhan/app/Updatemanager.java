package com.zuozhan.app;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.yixin.tinode.app.MyApp;

public class Updatemanager {
    static Updatemanager updatemanager = new Updatemanager();

    boolean isregister = false;

    private Updatemanager() {
    }

    public static Updatemanager getInstance() {
        return updatemanager;
    }


    public void register() {
        if (isregister){
            return;
        }
        isregister = true;
        MyApp.getInstance().registerReceiver(
                broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void unregister() {
        isregister = false;
        MyApp.getInstance().unregisterReceiver(broadcastReceiver);
    }

    BroadcastReceiver broadcastReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
//                    UpdateThreadPoolManager.getInstance()
//                            .executeTask(
//                                    new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Cursor cursor = null;
//                                            try {
//                                                final long completeDownloadId =
//                                                        intent.getLongExtra(
//                                                                DownloadManager.EXTRA_DOWNLOAD_ID,
//                                                                -1);
//                                                DownloadManager.Query query =
//                                                        new DownloadManager.Query();
//                                                DownloadManager downloadManager =
//                                                        (DownloadManager)
//                                                                MyApp.getInstance()
//                                                                        .getSystemService(
//                                                                                Context
//                                                                                        .DOWNLOAD_SERVICE);
//                                                cursor =
//                                                        downloadManager.query(
//                                                                query.setFilterById(
//                                                                        completeDownloadId));
//
//                                                if (cursor != null && cursor.moveToFirst()) {
//                                                    // 下载的文件到本地的目录
//                                                    String address =
//                                                            cursor.getString(
//                                                                    cursor.getColumnIndex(
//                                                                            DownloadManager
//                                                                                    .COLUMN_LOCAL_URI));
//                                                    LogUtil.d("address = " + address);
//                                                    String p = Uri.parse(address).getPath();
//                                                    context.startActivity(OpenFileUtil.openFile(p));
//                                                }
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            } finally {
//                                                if (cursor != null) {
//                                                    cursor.close();
//                                                }
//                                            }
//                                        }
//                                    });
                }
            };
}
