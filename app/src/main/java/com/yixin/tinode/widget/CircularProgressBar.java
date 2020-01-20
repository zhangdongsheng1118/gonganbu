package com.yixin.tinode.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 环形的进度条
 *
 * @author lwz <lwz0316@gmail.com>
 */
public class CircularProgressBar extends View {

    private int mDuration = 100;
    private int mProgress = -1;

    private Paint mPaint = new Paint();
    private RectF mRectF = new RectF();

    private int mBackgroundColor = Color.LTGRAY;
    private int mPrimaryColor = Color.parseColor("#6DCAEC");
    private float mStrokeWidth = 10F;

    /**
     * 进度条改变监听
     * <p>
     * {@link #onChange(int duration, int progress, float rate)}
     */
    public interface OnProgressChangeListener {
        /**
         * 进度改变事件，当进度条进度改变，就会调用该方法
         *
         * @param duration 总进度
         * @param progress 当前进度
         * @param rate     当前进度与总进度的商 即：rate = (float)progress / duration
         */
        public void onChange(int duration, int progress, float rate);
    }

    private OnProgressChangeListener mOnChangeListener;

    /**
     * 设置进度条改变监听
     *
     * @param l
     */
    public void setOnProgressChangeListener(OnProgressChangeListener l) {
        mOnChangeListener = l;
    }

    public CircularProgressBar(Context context) {
        super(context);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置进度条的最大值, 该值要 大于 0
     *
     * @param max
     */
    public void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        mDuration = max;
    }

    /**
     * 得到进度条的最大值
     *
     * @return
     */
    public int getMax() {
        return mDuration;
    }

    /**
     * 设置进度条的当前的值
     *
     * @param progress
     */
    public void setProgress(int progress) {
        if (progress > mDuration) {
            progress = mDuration;
        }
        mProgress = progress;
        if (mOnChangeListener != null) {
            mOnChangeListener.onChange(mDuration, progress, getRateOfProgress());
        }
        invalidate();
    }

    /**
     * 得到进度条当前的值
     *
     * @return
     */
    public int getProgress() {
        return mProgress;
    }

    /**
     * 设置进度条背景的颜色
     */
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    /**
     * 设置进度条进度的颜色
     */
    public void setPrimaryColor(int color) {
        mPrimaryColor = color;
    }

    /**
     * 设置环形的宽度
     *
     * @param width
     */
    public void setCircleWidth(float width) {
        mStrokeWidth = width;

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int halfWidth = getWidth() / 2;
        int halfHeight = getHeight() / 2;
        int radius = halfWidth < halfHeight ? halfWidth : halfHeight;
        float halfStrokeWidth = mStrokeWidth / 2;

        // 设置画笔
        mPaint.setColor(mBackgroundColor);
        mPaint.setDither(true);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);    //设置图形为空心

        // 画背景
        canvas.drawCircle(halfWidth, halfHeight, radius - halfStrokeWidth, mPaint);

        // 画当前进度的圆环
        mPaint.setColor(mPrimaryColor);    // 改变画笔颜色
        mRectF.top = halfHeight - radius + halfStrokeWidth;
        mRectF.bottom = halfHeight + radius - halfStrokeWidth;
        mRectF.left = halfWidth - radius + halfStrokeWidth;
        mRectF.right = halfWidth + radius - halfStrokeWidth;
        canvas.drawArc(mRectF, -90, getRateOfProgress() * 360, false, mPaint);

        // 画进度
        mPaint.setColor(Color.WHITE);    // 改变画笔颜色
        mPaint.setStrokeWidth(5);//设置画笔宽度
        mPaint.setAntiAlias(true);//设置是否使用抗锯齿功能，如果使用，会导致绘图速度变慢
        mPaint.setStyle(Paint.Style.FILL);//设置绘图样式，对于设置文字和几何图形都有效，可取值有三种 ：1、Paint.Style.FILL：填充内部 2、Paint.Style.FILL_AND_STROKE：填充内部和描边 3、Paint.Style.STROKE：仅描边
        mPaint.setTextAlign(Paint.Align.CENTER);//设置文字对齐方式
        mPaint.setTextSize(30);
        // 设置文字大小
        canvas.drawText((int) Math.ceil(getRateOfProgress() * 100) + "%", halfWidth, halfHeight + 5, mPaint);

        canvas.save();
    }

    /**
     * 得到当前的进度的比率
     * <p> 用进度条当前的值 与 进度条的最大值求商 </p>
     *
     * @return
     */
    private float getRateOfProgress() {
        return (float) mProgress / mDuration;
    }

}