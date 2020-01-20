package com.yixin.tinode.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.lqr.adapter.LQRHeaderAndFooterAdapter;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.adapter.OnItemClickListener;
import com.lqr.adapter.OnItemLongClickListener;
import com.lqr.adapter.OnItemTouchListener;
import com.yixin.tinode.R;

import java.util.List;

/**
 * RecyclerView通用的适配器（支持多itemType）
 */
public abstract class LQRAdapterForRecyclerViewMine<T> extends RecyclerView.Adapter<LQRViewHolderForRecyclerView> {

    private String TAG=LQRAdapterForRecyclerViewMine.class.getSimpleName();
    private Context mContext;
    private int mDefaultLayoutId = 0;
    private LQRHeaderAndFooterAdapter mHeaderAndFooterAdapter;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemTouchListener mOnItemTouchListener;

    public static final int SEND_TEXT = R.layout.item_text_send;
    public static final int RECEIVE_TEXT = R.layout.item_text_receive;
    public static final int SEND_IMAGE = R.layout.item_image_send;
    public static final int RECEIVE_IMAGE = R.layout.item_image_receive;
    public static final int SEND_IMAGE_INLINE = R.layout.item_image_send_inline;
    public static final int RECEIVE_IMAGE_INLINE = R.layout.item_image_receive_inline;
    public static final int SEND_STICKER = R.layout.item_sticker_send;
    public static final int RECEIVE_STICKER = R.layout.item_sticker_receive;
    public static final int SEND_VIDEO = R.layout.item_video_send;
    public static final int RECEIVE_VIDEO = R.layout.item_video_receive;
    public static final int SEND_LOCATION = R.layout.item_location_send;
    public static final int RECEIVE_LOCATION = R.layout.item_location_receive;
    public static final int RECEIVE_NOTIFICATION = R.layout.item_notification;
    public static final int RECEIVE_VOICE = R.layout.item_audio_receive;
    public static final int SEND_VOICE = R.layout.item_audio_send;
    public static final int UNDEFINE_MSG = R.layout.item_no_support_msg_type;
    public static final int RECALL_NOTIFICATION = R.layout.item_recall_notification;
    public static final int RECEIVE_FILE = R.layout.item_file_receive;
    public static final int SEND_FILE = R.layout.item_file_send;
    public static final int ADD_MEMBER = R.layout.item_notification_member;

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public OnItemTouchListener getOnItemTouchListener() {
        return mOnItemTouchListener;
    }

    public void setOnItemTouchListener(OnItemTouchListener onItemTouchListener) {
        mOnItemTouchListener = onItemTouchListener;
    }

    /**
     * 当使用多种itemType时，最好使用这种构造方法
     */
    public LQRAdapterForRecyclerViewMine(Context context) {
        mContext = context;
    }

    /**
     * 当使用一种itemType时，最好使用这种构造方法
     */
    public LQRAdapterForRecyclerViewMine(Context context, int defaultLayoutId) {
        this(context);
        mDefaultLayoutId = defaultLayoutId;
    }

    /**
     * 当需要使用多itemType时，请重写该方法，返回值就是对应类型的布局id
     */
    @Override
    public int getItemViewType(int position) {
        if (mDefaultLayoutId == 0) {
            throw new RuntimeException("请在 " + this.getClass().getSimpleName() + " 中重写 getItemViewType 方法返回布局资源 id，或者使用 " + this.getClass().getSimpleName() + " 三个参数的构造方法 LQRAdapterForRecyclerView(Context context, int defaultLayoutId, List<T> data)");
        }
        return mDefaultLayoutId;
    }

    private String getItemViewTypeString(int viewType){
        String typeString="";
        switch (viewType){
            case SEND_TEXT:
                typeString="SEND_TEXT";
                break;
            case RECEIVE_TEXT:
                typeString="RECEIVE_TEXT";
                break;
            case SEND_IMAGE:
                typeString="SEND_IMAGE";
                break;
            case RECEIVE_IMAGE:
                typeString="RECEIVE_IMAGE";
                break;
            case SEND_IMAGE_INLINE:
                typeString= "SEND_IMAGE_INLINE";
                break;
            case RECEIVE_IMAGE_INLINE:
                typeString= "RECEIVE_IMAGE_INLINE";
                break;
            case SEND_STICKER:
                typeString= "SEND_STICKER";
                break;
            case RECEIVE_STICKER:
                typeString= "RECEIVE_STICKER";
                break;
            case SEND_VIDEO:
                typeString= "SEND_VIDEO";
                break;
            case RECEIVE_VIDEO:
                typeString= "RECEIVE_VIDEO";
                break;
            case SEND_LOCATION:
                typeString= "SEND_LOCATION";
                break;
            case RECEIVE_LOCATION:
                typeString= "RECEIVE_LOCATION";
                break;
            case RECEIVE_NOTIFICATION:
                typeString= "RECEIVE_NOTIFICATION";
                break;
            case SEND_VOICE:
                typeString= "SEND_VOICE";
                break;
            case RECEIVE_VOICE:
                typeString= "RECEIVE_VOICE";
                break;
            case UNDEFINE_MSG:
                typeString= "UNDEFINE_MSG";
                break;
            case SEND_FILE:
                typeString= "SEND_FILE";
                break;
            case RECEIVE_FILE:
                typeString= "RECEIVE_FILE";
                break;
            case RECALL_NOTIFICATION:
                typeString= "RECALL_NOTIFICATION";
                break;
            case ADD_MEMBER:
                typeString= "ADD_MEMBER";
                break;
            default:
                typeString= "default??";
        }
        return typeString;
    }

    @Override
    public LQRViewHolderForRecyclerView onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG,"onCreateViewHolder viewType="+getItemViewTypeString(viewType));
        LQRViewHolderForRecyclerView holder = new LQRViewHolderForRecyclerView(mContext, View.inflate(mContext, viewType, null));
        if(holder==null){
            throw new NullPointerException();
        }
        holder.setOnItemClickListener(mOnItemClickListener);
        holder.setOnItemLongClickListener(mOnItemLongClickListener);
        holder.setOnItemTouchListener(mOnItemTouchListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(LQRViewHolderForRecyclerView holder, int position) {
        convert(holder, position);
    }

//    @Override
//    public void onBindViewHolder(LQRViewHolderForRecyclerView holder, int position, List<Object> payloads) {
//        convert(holder, mData.get(position), position);
//    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public abstract void convert(LQRViewHolderForRecyclerView helper, int position);

    /*================== 数据操作相关 begin ==================*/

    /**
     * 获取指定索引位置的数据模型
     */

    /**
     * 获取数据集合
     */

    /**
     * 数据局部刷新
     */
    public final void notifyItemRangeInsertedWrapper(int positionStart, int itemCount) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemRangeInserted(positionStart, itemCount);
        } else {
            mHeaderAndFooterAdapter.notifyItemRangeInserted(mHeaderAndFooterAdapter.getHeadersCount() + positionStart, itemCount);
        }
    }

    /**
     * 在集合头部添加新的数据集合（下拉从服务器获取最新的数据集合）
     */
    public void addNewData(List<T> data) {
        if (data != null) {
//            mData.addAll(0, data);
            notifyItemRangeInsertedWrapper(0, data.size());
        }
    }

    /**
     * 在集合尾部添加更多数据集合（上拉从服务器获取更多的数据集合）
     */
    public void addMoreData(List<T> data) {
        if (data != null) {
//            mData.addAll(mData.size(), data);
//            notifyItemRangeInsertedWrapper(mData.size(), data.size());
        }
    }

    /**
     * 数据全局刷新
     */
    public final void notifyDataSetChangedWrapper() {
        if (mHeaderAndFooterAdapter == null) {
            notifyDataSetChanged();
        } else {
            mHeaderAndFooterAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置全新的数据集合，如果传入null，则清空数据列表（第一次从服务器加载数据，或者下拉刷新当前界面数据列表）
     */
    public void setData(List<T> data) {
        if (data != null) {
//            mData = data;
        } else {
//            mData.clear();
        }
        notifyDataSetChangedWrapper();
    }

    /**
     * 清空数据列表
     */
    public void clearData() {
//        mData.clear();
        notifyDataSetChangedWrapper();
    }

    /**
     * 数据移除刷新
     */
    public final void notifyItemRemoveWrapper(int position) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemRemoved(position);
        } else {
            mHeaderAndFooterAdapter.notifyItemRemoved(mHeaderAndFooterAdapter.getHeadersCount() + position);
        }
    }

    /**
     * 删除指定索引数据条目
     */
    public void removeItem(int position) {
//        mData.remove(position);
        notifyItemRemoveWrapper(position);
    }

    /**
     * 删除指定数据条目
     */
    public void removeItem(T model) {
//        removeItem(mData.indexOf(model));
    }

    /**
     * 数据添加刷新
     */
    public final void notifyItemInsertedWrapper(int position) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemInserted(position);
        } else {
            mHeaderAndFooterAdapter.notifyItemInserted(mHeaderAndFooterAdapter.getHeadersCount() + position);
        }
    }

    /**
     * 在指定位置添加数据条目
     */
    public void addItem(int position, T model) {
//        mData.add(position, model);
        notifyItemInsertedWrapper(position);
    }

    /**
     * 在集合头部添加数据条目
     */
    public void addFirstItem(T model) {
        addItem(0, model);
    }

    /**
     * 在集合末尾添加数据条目
     */
    public void addLastItem(T model) {
//        addItem(mData.size(), model);
    }

    /**
     * 数据变化刷新
     */
    public final void notifyItemChangedWrapper(int position) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemChanged(position);
        } else {
            mHeaderAndFooterAdapter.notifyItemChanged(mHeaderAndFooterAdapter.getHeadersCount() + position);
        }
    }

    /**
     * 替换指定索引的数据条目
     */
    public void setItem(int position, T newModel) {
//        mData.set(position, newModel);
        notifyItemChangedWrapper(position);
    }

    /**
     * 替换指定数据条目
     */
    public void setItem(T oldModel, T newModel) {
//        setItem(mData.indexOf(oldModel), newModel);
    }

    /**
     * 数据移动刷新
     */
    public final void notifyItemMoveWrapper(int fromPosition, int toPosition) {
        if (mHeaderAndFooterAdapter == null) {
            notifyItemMoved(fromPosition, toPosition);
        } else {
            mHeaderAndFooterAdapter.notifyItemMoved(mHeaderAndFooterAdapter.getHeadersCount() + fromPosition, mHeaderAndFooterAdapter.getHeadersCount() + toPosition);
        }
    }

    /**
     * 移动数据条目的位置
     */
    public void moveItem(int fromPosition, int toPosition) {
        notifyItemChangedWrapper(fromPosition);
        notifyItemChangedWrapper(toPosition);

        //要先执行上面的notifyItemChanged，然后再执行下面的moveItem事件

//        mData.add(toPosition, mData.remove(fromPosition));
        notifyItemMoveWrapper(fromPosition, toPosition);
    }

    /**
     * 获取第一个数据模型
     */
//    public T getFirstItem() {
//        return getItemCount() > 0 ? getItem(0) : null;
//    }

    /**
     * 得到最后一个数据模型
     */
//    public T getLastItem() {
//        return getItemCount() > 0 ? getItem(getItemCount() - 1) : null;
//    }

    /*================== 数据操作相关 end ==================*/

    /*================== 头部、尾部部相关 begin ==================*/

    public void addHeaderView(View headerView) {
        getHeaderAndFooterAdapter().addHeaderView(headerView);
    }

    public void addFooterView(View footerView) {
        getHeaderAndFooterAdapter().addFooterView(footerView);
    }

    public int getHeadersCount() {
        return mHeaderAndFooterAdapter == null ? 0 : mHeaderAndFooterAdapter.getHeadersCount();
    }

    public int getFootersCount() {
        return mHeaderAndFooterAdapter == null ? 0 : mHeaderAndFooterAdapter.getFootersCount();
    }

    /**
     * 判断是否是头部或尾部
     */
    public boolean isHeaderOrFooter(RecyclerView.ViewHolder viewHolder) {
        return viewHolder.getAdapterPosition() < getHeadersCount() || viewHolder.getAdapterPosition() >= getHeadersCount() + getItemCount();
    }

    public LQRHeaderAndFooterAdapter getHeaderAndFooterAdapter() {
        if (mHeaderAndFooterAdapter == null) {
            synchronized (LQRHeaderAndFooterAdapter.class) {
                if (mHeaderAndFooterAdapter == null) {
                    mHeaderAndFooterAdapter = new LQRHeaderAndFooterAdapter(this);
                }
            }
        }
        return mHeaderAndFooterAdapter;
    }

    /*================== 头部、尾部相关 end ==================*/
}
