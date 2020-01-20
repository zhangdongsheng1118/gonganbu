package com.yixin.tinode.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.yixin.tinode.R;
import com.yixin.tinode.ui.fragment.ImageViewFragment;


/**
 *
 */
public class ContentActivity extends AppCompatActivity {

    public static final String KEY_FRAGMENT = "key_fragment";
    public static final String KEY_SLUG = "key_slug";

    public static final int FRAGMENT_VIEW_IMAGE = 0X01;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);

        swichFragment(getIntent());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void swichFragment(Intent intent) {
        int fragmentKey = intent.getIntExtra(KEY_FRAGMENT, FRAGMENT_VIEW_IMAGE);
        switch (fragmentKey) {
            case FRAGMENT_VIEW_IMAGE:
                replaceFragment(ImageViewFragment.newInstance(intent.getExtras()));
                break;
        }
    }

    public void replaceFragment(Fragment fragmnet) {
        replaceFragment(R.id.fragmentContent, fragmnet);
    }

    public void replaceFragment(@IdRes int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(id, fragment).commit();
    }
}
