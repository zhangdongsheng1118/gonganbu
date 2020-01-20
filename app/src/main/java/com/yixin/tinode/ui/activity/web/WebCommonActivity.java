package com.yixin.tinode.ui.activity.web;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.yixin.tinode.R;


/**
 * source code  https://github.com/Justson/AgentWeb
 */

public class WebCommonActivity extends AppCompatActivity {

    private String url;
    private FrameLayout mFrameLayout;
    public static final String TYPE_KEY = "type_key";
    public static final String URL_KEY = "url_key";
    private FragmentManager mFragmentManager;
    public static final int FLAG_GUIDE_DICTIONARY_USE_IN_ACTIVITY = 0x01;
    public static final int FLAG_GUIDE_DICTIONARY_BOUNCE_EFFACT = FLAG_GUIDE_DICTIONARY_USE_IN_ACTIVITY << 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common);

        mFrameLayout = (FrameLayout) this.findViewById(R.id.container_framelayout);
        Intent intent = getIntent();
        int key = intent.getIntExtra(TYPE_KEY, -1);
        url = intent.getStringExtra(URL_KEY);
        mFragmentManager = this.getSupportFragmentManager();
        openFragment(key);
    }


    private AgentWebFragment mAgentWebFragment;

    private void openFragment(int key) {

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        Bundle mBundle = null;

        switch (key) {

            /*回弹效果*/
            case FLAG_GUIDE_DICTIONARY_BOUNCE_EFFACT:
                ft.add(R.id.container_framelayout, mAgentWebFragment = BounceWebFragment.getInstance(mBundle = new Bundle()), BounceWebFragment.class.getName());
                mBundle.putString(AgentWebFragment.URL_KEY, url);
                break;
        }
        ft.commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //一定要保证 mAentWebFragemnt 回调
//		mAgentWebFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        AgentWebFragment mAgentWebFragment = this.mAgentWebFragment;
        if (mAgentWebFragment != null) {
            FragmentKeyDown mFragmentKeyDown = mAgentWebFragment;
            if (mFragmentKeyDown.onFragmentKeyDown(keyCode, event)) {
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
