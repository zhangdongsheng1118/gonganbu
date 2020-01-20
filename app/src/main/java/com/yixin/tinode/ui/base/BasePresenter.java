package com.yixin.tinode.ui.base;

import com.zuozhan.app.activity.AllBaseActivity;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class BasePresenter<V> {

    /*================== 以下是网络请求接口 ==================*/

    public AllBaseActivity mContext;

    public BasePresenter(AllBaseActivity context) {
        mContext = context;
    }

    protected Reference<V> mViewRef;

    public void attachView(V view) {
        mViewRef = new WeakReference<V>(view);
    }

    public boolean isViewAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }

    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }

    public V getView() {
        return mViewRef != null ? mViewRef.get() : null;
    }

}
