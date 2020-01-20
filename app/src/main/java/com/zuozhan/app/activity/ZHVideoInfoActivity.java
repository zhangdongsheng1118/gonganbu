package com.zuozhan.app.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.xiao.nicevideoplayer.NiceVideoPlayer;
import com.xiao.nicevideoplayer.NiceVideoPlayerManager;
import com.xiao.nicevideoplayer.TxVideoPlayerController;
import com.yixin.tinode.R;
import com.zuozhan.app.imageloader.ImageLoader;

public class ZHVideoInfoActivity extends AllBaseActivity {

    NiceVideoPlayer mNiceVideoPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_video_play);
        String url = getIntent().getStringExtra("data");
        String image = getIntent().getStringExtra("image");
//        String url = "http://47.111.141.221:10080/record/video/play/58/20190712194413/20190712194523";
//        String image = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg";
        String title = getIntent().getStringExtra("title");
        mNiceVideoPlayer =  findViewById(R.id.nice_video_player);
        mNiceVideoPlayer.setPlayerType(NiceVideoPlayer.TYPE_IJK); // or NiceVideoPlayer.TYPE_NATIVE
        mNiceVideoPlayer.setUp(url, null);

        TxVideoPlayerController controller = new TxVideoPlayerController(this);
        if (title == null){
            title = "";
        }
        controller.setTitle(title);
//        controller.setLenght(98000);
        controller.imageView().setImageDrawable(null);
        controller.imageView().setBackground(null);
        if (image == null){
            image = "";
        }
        ImageLoader.loadImage(this, controller.imageView(), image,R.drawable.tu);
        mNiceVideoPlayer.setController(controller);

    }

    @Override
    protected void onStop() {
        super.onStop();
        // 在onStop时释放掉播放器
        NiceVideoPlayerManager.instance().releaseNiceVideoPlayer();
    }

    @Override
    public void onBackPressed() {
        // 在全屏或者小窗口时按返回键要先退出全屏或小窗口，
        // 所以在Activity中onBackPress要交给NiceVideoPlayer先处理。
        if (NiceVideoPlayerManager.instance().onBackPressd()) return;
        super.onBackPressed();
    }
}
