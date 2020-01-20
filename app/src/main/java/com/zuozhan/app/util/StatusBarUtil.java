//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.zuozhan.app.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout.LayoutParams;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StatusBarUtil {
    private static final int FAKE_STATUS_BAR_VIEW_ID;
    private static final int FAKE_TRANSLUCENT_VIEW_ID;
    private static final int TAG_KEY_HAVE_SET_OFFSET = -123;
    private static final int HALF_STATUS_BAR_ALPHA = 85;
    private static Method mSetStatusBarDarkIcon;

    public StatusBarUtil() {}

    /**
     * 非沉浸式,状态栏
     */
    public static void setStatusBarForColor(
            Activity activity,
            @IntRange(from = 0L, to = 255L) int statusBarAlpha,
            boolean isTextBlack,
            @ColorInt int color) {
        if (activity != null) {
            if (VERSION.SDK_INT >= 19) {
                ViewGroup parent = (ViewGroup) activity.findViewById(android.R.id.content);
                View view = parent.getChildAt(0);
                addViewColor(activity, 0, color);
                setTranslucentForImageView(activity, statusBarAlpha, isTextBlack, view);
            }
        }
    }

    /**
     * 沉浸式,状态栏
     */
    public static void setStatusBarForFakeBarView(
            Activity activity,
            @IntRange(from = 0L, to = 255L) int statusBarAlpha,
            boolean isTextBlack) {
        if (activity != null) {
            if (VERSION.SDK_INT >= 19) {
                setTranslucentForImageView(activity, statusBarAlpha, isTextBlack, (View) null);
            }
        }
    }

    private static void setTransparentForWindow(Activity activity, boolean isTextBlack) {
        if (activity != null) {
            if (VERSION.SDK_INT >= 21) {
                activity.getWindow().addFlags(-2147483648);
                activity.getWindow().clearFlags(67108864);
                activity.getWindow().setStatusBarColor(0);
                if (VERSION.SDK_INT >= 23) {
                    activity.getWindow()
                            .getDecorView()
                            .setSystemUiVisibility((isTextBlack ? 8192 : 0) | 256 | 1024);
                    setMIUIStatusBarDarkIcon(activity, isTextBlack);
                    setMeizuStatusBarDarkIcon(activity, isTextBlack);
                } else {
                    activity.getWindow().getDecorView().setSystemUiVisibility(1280);
                }
            } else if (VERSION.SDK_INT >= 19) {
                activity.getWindow().setFlags(67108864, 67108864);
            }
        }
    }

    private static void setTranslucentForImageView(
            Activity activity,
            @IntRange(from = 0L, to = 255L) int statusBarAlpha,
            boolean isTextBlack,
            View needOffsetView) {
        if (activity != null) {
            if (VERSION.SDK_INT >= 19) {
                setTransparentForWindow(activity, isTextBlack);
                if (isCannotBlack() && statusBarAlpha == 0) {
                    statusBarAlpha = 85;
                }

                if (statusBarAlpha > 0) {
                    addTranslucentView(activity, statusBarAlpha);
                }

                if (needOffsetView != null) {
                    Object haveSetOffset = needOffsetView.getTag(-123);
                    if (haveSetOffset != null && (Boolean) haveSetOffset) {
                        return;
                    }

                    MarginLayoutParams layoutParams =
                            (MarginLayoutParams) needOffsetView.getLayoutParams();
                    layoutParams.setMargins(
                            layoutParams.leftMargin,
                            layoutParams.topMargin + getStatusBarHeight(activity),
                            layoutParams.rightMargin,
                            layoutParams.bottomMargin);
                    needOffsetView.setTag(-123, true);
                }
            }
        }
    }

    private static void addViewColor(
            Activity activity,
            @IntRange(from = 0L, to = 255L) int statusBarAlpha,
            @ColorInt int color) {
        if (activity != null) {
            ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
            View fakeStatusBarView = contentView.findViewById(FAKE_STATUS_BAR_VIEW_ID);
            if (fakeStatusBarView != null) {
                if (fakeStatusBarView.getVisibility() == View.GONE) {
                    fakeStatusBarView.setVisibility(View.VISIBLE);
                }

                fakeStatusBarView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha));
            } else {
                contentView.addView(createStatusBarViewColor(activity, color, statusBarAlpha));
            }
        }
    }

    private static View createStatusBarViewColor(
            Activity activity, @ColorInt int color, int alpha) {
        View statusBarView = new View(activity);
        LayoutParams params = new LayoutParams(-1, getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(calculateStatusColor(color, alpha));
        statusBarView.setId(FAKE_STATUS_BAR_VIEW_ID);
        return statusBarView;
    }

    private static void addTranslucentView(
            Activity activity, @IntRange(from = 0L, to = 255L) int statusBarAlpha) {
        if (activity != null) {
            ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
            View fakeTranslucentView = contentView.findViewById(FAKE_TRANSLUCENT_VIEW_ID);
            if (fakeTranslucentView != null) {
                if (fakeTranslucentView.getVisibility() == View.GONE) {
                    fakeTranslucentView.setVisibility(View.VISIBLE);
                }

                fakeTranslucentView.setBackgroundColor(Color.argb(statusBarAlpha, 0, 0, 0));
            } else {
                contentView.addView(createStatusBarViewTranslucent(activity, statusBarAlpha));
            }
        }
    }

    private static View createStatusBarViewTranslucent(Activity activity, int alpha) {
        View statusBarView = new View(activity);
        LayoutParams params = new LayoutParams(-1, getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        statusBarView.setId(FAKE_TRANSLUCENT_VIEW_ID);
        return statusBarView;
    }



    private static void setMIUIStatusBarDarkIcon(@NonNull Activity activity, boolean darkIcon) {
        Class clazz = activity.getWindow().getClass();

        try {
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", Integer.TYPE, Integer.TYPE);
            extraFlagField.invoke(activity.getWindow(), darkIcon ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception var7) {;
        }
    }

    private static void setMeizuStatusBarDarkIcon(@NonNull Activity activity, boolean dark) {
        try {
            mSetStatusBarDarkIcon = Activity.class.getMethod("setStatusBarDarkIcon", Boolean.TYPE);
        } catch (NoSuchMethodException var5) {;
        }

        if (mSetStatusBarDarkIcon != null) {
            try {
                mSetStatusBarDarkIcon.invoke(activity, dark);
            } catch (IllegalAccessException var3) {
                var3.printStackTrace();
            } catch (InvocationTargetException var4) {
                var4.printStackTrace();
            }
        }
    }

    private static int calculateStatusColor(@ColorInt int color, int alpha) {
        if (alpha == 0) {
            return color;
        } else {
            float a = 1.0F - (float) alpha / 255.0F;
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            red = (int) ((double) ((float) red * a) + 0.5D);
            green = (int) ((double) ((float) green * a) + 0.5D);
            blue = (int) ((double) ((float) blue * a) + 0.5D);
            return -16777216 | red << 16 | green << 8 | blue;
        }
    }

    private static int dipToPx(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }

    public static int getStatusBarHeight(Context context) {
        boolean var1 = true;

        int result;
        try {
            int resourceId =
                    context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            result = context.getResources().getDimensionPixelSize(resourceId);
        } catch (Exception var3) {
            result = dipToPx(context, 24.0F);
        }

        return result;
    }

    private static boolean isCannotBlack() {
        boolean isCannotBlack = false;
        if (Build.MODEL != null && Build.MODEL.contains("ZUK")) {
            isCannotBlack = true;
        }

        return isCannotBlack;
    }

    static {
//        FAKE_STATUS_BAR_VIEW_ID = R.id.view_fake_status_bar;
//        FAKE_TRANSLUCENT_VIEW_ID = R.id.view_fake_translucent;
        FAKE_STATUS_BAR_VIEW_ID = -1;
        FAKE_TRANSLUCENT_VIEW_ID = -1;
    }
}
