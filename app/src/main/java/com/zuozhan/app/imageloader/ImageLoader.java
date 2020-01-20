package com.zuozhan.app.imageloader;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageLoader {

    public static void loadImage(Context context, ImageView imageView, String url, int def,String colorId) {
        Glide.with(context).load(url).placeholder(def).error(def).into(imageView);
        if (!TextUtils.isEmpty(colorId)){
            imageView.setBackgroundColor(Color.parseColor(colorId));
        }
    }
    public static void loadImage(Context context, ImageView imageView, String url, int def) {
        Glide.with(context).load(url).placeholder(def).error(def).into(imageView);
    }

    public static void loadImage(Fragment context, ImageView imageView, String url) {
        Glide.with(context).load(url).into(imageView);
    }


    public static void loadImage(Fragment context, ImageView imageView, int url) {
        Glide.with(context).load(url).into(imageView);
    }

    public static void loadImage(Context context, ImageView imageView, String url) {
        Glide.with(context).load(url).into(imageView);
    }

    public static void loadImage(Context context, ImageView imageView, int url) {
        Glide.with(context).load(url).into(imageView);
    }

}
