package com.yixin.tinode.ui.adapter;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.gson.Gson;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.adapter.OnItemClickListener;
import com.lqr.adapter.OnItemLongClickListener;
import com.lqr.emoji.MoonUtils;
import com.scoopit.weedfs.client.outer.UploadAndGetUrl;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.db.tinode.MessageDb;
import com.yixin.tinode.db.tinode.StoredMessage;
import com.yixin.tinode.model.data.LocationData;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.media.SpanFormatter;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.UserInfoActivity;
import com.yixin.tinode.ui.presenter.SessionAtPresenter;
import com.yixin.tinode.util.FileUtils;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.MediaFileUtils;
import com.yixin.tinode.util.TimeUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.widget.BubbleImageView;
import com.yixin.tinode.widget.CircularProgressBar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import co.tinode.tinodesdk.MeTopic;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.Drafty;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;

/**
 * 显示聊天历史
 * 发送消息
 * 接收消息
 *
 * @创建者 CSDN_LQR
 * @描述 会话界面的消息列表适配器
 */
public class SessionAdapter extends LQRAdapterForRecyclerViewMine<StoredMessage> {
    private static String TAG = "SESSIONADAPTER";

    private FragmentActivity mContext;
    private SessionAtPresenter mPresenter;
    private List<StoredMessage> mData = new ArrayList<>(20);

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
    public static final int RECEIVE_RED_PACKET = R.layout.item_red_packet_receive;
    public static final int SEND_RED_PACKET = R.layout.item_red_packet_send;
    public static final int UNDEFINE_MSG = R.layout.item_no_support_msg_type;
    public static final int RECALL_NOTIFICATION = R.layout.item_recall_notification;
    public static final int RECEIVE_FILE = R.layout.item_file_receive;
    public static final int SEND_FILE = R.layout.item_file_send;
    public static final int ADD_MEMBER = R.layout.item_notification_member;

    private RecyclerView mRecyclerView;

    public SessionAdapter(FragmentActivity context, SessionAtPresenter presenter) {
        super(context);
        mContext = context;
        mPresenter = presenter;
        mLoaderCallbacks = new MessageLoaderCallbacks();
        mPagesToLoad = 1;

        mLoaders = new LongSparseArray<>();
    }

    public void setmPagesToLoad(int mPagesToLoad) {
        this.mPagesToLoad = mPagesToLoad;
    }

    @Override
    public void onBindViewHolder(LQRViewHolderForRecyclerView holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            convert(holder, position);
        } else {
            Object payload = payloads.get(0);
            BuglyLog.d(TAG,"payload:"+new Gson().toJson(payload));
            if (payload instanceof UploadAndGetUrl.FileInfo) {
                convert(holder, position, (UploadAndGetUrl.FileInfo) payload);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return MessageDb.getLocalId(mCursor);
    }

    @Override
    public int getItemCount() {
        int count = mCursor != null ? mCursor.getCount() : 0;
        if (count == 0) {
            mData.clear();

            BuglyLog.d(TAG,"getItemCount mData.clear()");
        }
        return count;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    @Override
    public void convert(LQRViewHolderForRecyclerView helper, int position) {
        Log.d(TAG, "position is " + position);
        StoredMessage item = getMessage(position);
        if(item==null){
            BuglyLog.d(TAG,"can't get StoredMessage by position!");
            throw new NullPointerException();
        }

        //暂时不知道此处的作用，先注掉。影响了加群和删除成员的消息显示
        //        if(StringUtils.isBlank(item.search) ){
        //            return;
        //        }
        setTime(helper, item, position);
        setView(helper, item, position);

        int viewType = getItemViewType(item);
        if ((viewType != RECEIVE_NOTIFICATION) && (viewType != RECALL_NOTIFICATION) && (viewType != UNDEFINE_MSG) && (viewType != ADD_MEMBER)) {
            setAvatar(helper, item, position);
            //            setName(helper, item, position);
            setStatus(helper, item, position, null);
            setOnClick(helper, item, position);
        }
    }

    private void convert(LQRViewHolderForRecyclerView helper, int position, UploadAndGetUrl.FileInfo fileInfo) {
        StoredMessage item = getMessage(position);
        setTime(helper, item, position);
        setView(helper, item, position);

        int viewType = getItemViewType(item);
        if ((viewType != RECEIVE_NOTIFICATION) && (viewType != RECALL_NOTIFICATION) && (viewType != UNDEFINE_MSG) && (viewType != ADD_MEMBER)) {
            setAvatar(helper, item, position);
            //            setName(helper, item, position);
            setStatus(helper, item, position, fileInfo);
            setOnClick(helper, item, position);
        }
    }

    public List<StoredMessage> getData() {
        return mData;
    }

    public int getPostionByMsgId(long msgId) {
        int position = -1;

        if (mData.size() > 0) {
            for (int index = mData.size() - 1; index >= 0; --index) {
                StoredMessage msg = mData.get(index);
                if (msg != null && msg.getId() == msgId) {
                    position = index;
                    break;
                } else {
                }
            }
        }

        if (position < 0 && mCursor != null) {
            int count = mCursor.getCount();
            for (int index = 0; index < count; ++index) {
                StoredMessage msg = getMessage(index);
                if (msg != null && msg.getId() == msgId) {
                    position = index;
                    break;
                }
            }
        }
        return position;
    }

    public static FileInfo getFileInfo(Drafty drafty) {
        FileInfo fileInfo = null;
        Drafty.Style[] fmt = drafty.getStyles();
        if (fmt != null) {
            Drafty.Style style = fmt[0];

            fileInfo = new FileInfo();
            Map<String, Object> map = drafty.getEntity(style).getData();
            Log.d(TAG,"drafty entity="+new Gson().toJson(map));

            fileInfo.type = (Integer) map.get("type");
            fileInfo.url = (String) map.get("ref");
            if(fileInfo.url!=null){
                fileInfo.url = fileInfo.genUrl(fileInfo.type, fileInfo.url);
            }
            Object length = map.get("length");
            if (length != null) {
                fileInfo.size = Long.parseLong(length.toString());
            }
            fileInfo.name = (String) map.get("name");
            fileInfo.mime = (String) map.get("mime");
            fileInfo.cover = (String) map.get("cover");
            fileInfo.local = (String) map.get("local");
            fileInfo.val = map.get("val");
        }

        return fileInfo;
    }

    public static class FileInfo {
        public long msgId;
        public String name;
        public long size;
        public String mime;
        public String url;
        public String cover;
        public String local;
        //文件内容
        public Object val;
        //发送方式
        public Integer type;

        /**
         * 生成带前缀的路径
         */
        public static String genUrl(Integer type, String url) {
            String real = url;
            try {
                if (type == null || type == Drafty.SEND_FILE_TYPE_TINODE) {
                    real = new URL(Cache.getTinode().getBaseUrl(), url).toString();
                } else {
                    real = url.startsWith("http") ? url : "http://" + url;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return real;
        }

        public int getResIdByMime() {
            if (name.contains(".xls") || name.contains(".xlsx")) {

                return R.drawable.ic_excel;
            } else if (name.contains(".doc") || name.contains(".docx")) {

                return R.drawable.ic_word;
            } else if (name.contains(".pdf")) {

                return R.drawable.ic_pdf;
            } else if (name.contains(".zip")) {

                return R.drawable.ic_zip;
            } else if (name.contains(".txt")) {

                return R.drawable.ic_txt;
            } else {
                return R.drawable.ic_dir;
            }
        }
    }

    private class NoLineClickSpan extends ClickableSpan {
        String color;
        String topic;

        public NoLineClickSpan(String color, String topic) {
            super();
            this.color = color;
            this.topic = topic;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            //设置字体颜色
            ds.setColor(Color.parseColor(color));
            ds.setUnderlineText(false); //去掉下划线
        }

        @Override
        public void onClick(View widget) {
            //点击超链接时调用
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            intent.putExtra("topic", topic);
            mContext.startActivity(intent);
        }
    }

    public static LocationData parseStoredMsgToLocationData(StoredMessage msg) {
        Map<String, Object> info = msg.content.getInfo();
        LocationData data = new LocationData(Double.parseDouble(info.get("lat").toString()), Double.parseDouble(info.get("lng").toString()), (String) info.get("name"), (String) info.get("val"));
        return data;
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

    private void setView(LQRViewHolderForRecyclerView helper, StoredMessage item, int position) {
        //根据消息类型设置消息显示内容
        int viewType = getItemViewType(item);
        BuglyLog.d(TAG,"setView type="+getItemViewTypeString(viewType));
        Drafty msgContent = item.content;
        if (viewType == SEND_TEXT || viewType == RECEIVE_TEXT) {
            TextView tv = helper.getView(R.id.tvText);
            Log.e(TAG, "setView SEND_TEXT position:" + position + " msg:" + msgContent.txt);
            if (tv == null) {
                Log.e(TAG, "setView SEND_TEXT fuck tv=null,position:" + position + " msg:" + msgContent.txt);

                CrashReport.postCatchedException(new Exception("send_text R.id.tvText is null, " + new Gson().toJson(msgContent)));
                //todo 暂时容错，需要复现步骤
                return;
            }
            MoonUtils.identifyFaceExpression(mContext, tv, msgContent.txt, ImageSpan.ALIGN_BOTTOM);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListenerMine != null) {
                        mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                    }
                }
            });
            tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return mOnItemLongClickListenerMine != null ?
                            mOnItemLongClickListenerMine.onItemLongClick(helper, (ViewGroup) null, view, position) : false;
                }
            });

            ImageView ivErr = helper.getView(R.id.ivError);
            ivErr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListenerMine != null) {
                        mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                    }
                }
            });
        } else if (viewType == SEND_IMAGE_INLINE || viewType == RECEIVE_IMAGE_INLINE) {
            BubbleImageView bivPic = helper.getView(R.id.bivPic);
            if(bivPic==null){
                return;
            }
            Drafty.Style[] fmt = msgContent.getStyles();

            if (fmt != null) {
                if (item.seq <= 0) {
                    LogUtils.w("seq <0 img path");
                } else {
                    bivPic.setImageDrawable(new BitmapDrawable(mContext.getResources(), SpanFormatter.handleImage(mContext, msgContent.getInfo())));
                }

                bivPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListenerMine != null) {
                            mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                        }
                    }
                });
                bivPic.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return mOnItemLongClickListenerMine != null ?
                                mOnItemLongClickListenerMine.onItemLongClick(helper, (ViewGroup) null, view, position) : false;
                    }
                });
            }
        } else if (viewType == SEND_IMAGE || viewType == RECEIVE_IMAGE) {
            BubbleImageView bivPic = helper.getView(R.id.bivPic);
            if(bivPic==null){
                return;
            }
            Drafty.Style[] fmt = msgContent.getStyles();

            if (fmt != null) {
                Drafty.Style style = fmt[0];
                Map<String, Object> data = msgContent.getEntity(style).getData();

                String image = (String) data.get("val");
                String localImage=(String)data.get("local");
                if (item.seq <= 0) {
                    LogUtils.w("seq <0 img path" + image);
                } else {
                    LogUtils.w("seq img path:" + image);
                    //判断是否有本地图片,若有,则直接显示本地的,先暂时加上viewType判断,最终不需要
                    if (localImage != null&&viewType == SEND_IMAGE) {
                        Glide.with(mContext).load(localImage).error(R.mipmap.default_img_failed)
                                .override(UIUtils.dip2Px(150), UIUtils.dip2Px(300))
                                .fitCenter()
                                .into(bivPic);
                    }
                    //否则加载网络的图片, 并且需要存到本地,这样下次再判断就直接显示本地的了
                    else{
                        try {
                            String url = msgContent.getImageUrl(true, Cache.getTinode().getBaseUrl());
                            if (url == null) {
                                //todo 后台监控到有时为null，此处暂做兼容处理。需调查原因
                                String error = "图片为空:" + new Gson().toJson(data);
                                LogUtils.e(error);
                                CrashReport.postCatchedException(new Exception(error));
                                url = "error";
                            }

                            Glide.with(mContext).load(new GlideUrl(url, new LazyHeaders.Builder()
                                    .addHeader("X-Tinode-APIKey", Cache.getTinode().getApiKey())
                                    .addHeader("Authorization", "Token " + Cache.getTinode().getAuthToken())
                                    .build())).error(R.mipmap.default_img_failed)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .override(UIUtils.dip2Px(150), UIUtils.dip2Px(300))
                                    .fitCenter()
                                    .into(bivPic);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }

                }

                bivPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListenerMine != null) {
                            mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                        }
                    }
                });
                bivPic.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return mOnItemLongClickListenerMine != null ?
                                mOnItemLongClickListenerMine.onItemLongClick(helper, (ViewGroup) null, view, position) : false;
                    }
                });

                ImageView ivErr = helper.getView(R.id.ivError);
                ivErr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListenerMine != null) {
                            mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                        }
                    }
                });
            }
        } else if (viewType == SEND_FILE || viewType == RECEIVE_FILE) {
            FileInfo fileInfo = getFileInfo(msgContent);
            ((TextView) helper.getView(R.id.tvText)).setText(fileInfo.name);
            ((TextView) helper.getView(R.id.tvFileSize)).setText(FileUtils.formateFileSize(fileInfo.size));
            ((ImageView) helper.getView(R.id.iv_file_icon)).setImageResource(fileInfo.getResIdByMime());
            helper.getView(R.id.rl_file).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListenerMine != null) {
                        mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                    }
                }
            });
            helper.getView(R.id.rl_file).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return mOnItemLongClickListenerMine != null ?
                            mOnItemLongClickListenerMine.onItemLongClick(helper, (ViewGroup) null, view, position) : false;
                }
            });
            ImageView ivErr = helper.getView(R.id.ivError);
            ivErr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListenerMine != null) {
                        mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                    }
                }
            });
        } else if (viewType == SEND_STICKER || viewType == SEND_VIDEO || viewType == RECEIVE_STICKER || viewType == RECEIVE_VIDEO) {
            FileInfo fileInfo = getFileInfo(msgContent);
            if (MediaFileUtils.isImageFileType(fileInfo.name)) {
                ImageView ivPic = helper.getView(R.id.ivSticker);
                Glide.with(mContext).load(fileInfo.url).placeholder(R.mipmap.default_img).error(R.mipmap.default_img_failed).centerCrop().into(ivPic);

                ivPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListenerMine != null) {
                            mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                        }
                    }
                });
                ivPic.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return mOnItemLongClickListenerMine != null ?
                                mOnItemLongClickListenerMine.onItemLongClick(helper, (ViewGroup) null, view, position) : false;
                    }
                });

                ImageView ivErr = helper.getView(R.id.ivError);
                ivErr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListenerMine != null) {
                            mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                        }
                    }
                });
            } else if (MediaFileUtils.isVideoFileType(fileInfo.name)) {
                BubbleImageView bivPic = helper.getView(R.id.bivPic);
                if(bivPic==null){
                    return;
                }
                String image = fileInfo.cover;
                Log.d(TAG,"==========================  image=" + image);
                if (fileInfo.cover != null) {
                    String url = FileInfo.genUrl(fileInfo.type, image);
                    Glide.with(mContext).load(new GlideUrl(url, new LazyHeaders.Builder()
                            .addHeader("X-Tinode-APIKey", Cache.getTinode().getApiKey())
                            .addHeader("Authorization", "Token " + Cache.getTinode().getAuthToken())
                            .build())).error(R.mipmap.img_video_default)
                            .override(UIUtils.dip2Px(150), UIUtils.dip2Px(300))
                            .fitCenter()
                            .into(bivPic);
                    helper.setViewVisibility(R.id.ivPlay, View.VISIBLE);
                } else {
                    bivPic.setImageResource(R.mipmap.img_video_default);
                    if(helper==null){
                        BuglyLog.d(TAG,"WOW,helper is null!!");
                    }
                    BuglyLog.d(TAG,"viewType====="+viewType);
                    helper.setViewVisibility(R.id.ivPlay, View.GONE);
                }

                bivPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListenerMine != null) {
                            mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                        }
                    }
                });
                bivPic.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return mOnItemLongClickListenerMine != null ?
                                mOnItemLongClickListenerMine.onItemLongClick(helper, (ViewGroup) null, view, position) : false;
                    }
                });

                ImageView ivErr = helper.getView(R.id.ivError);
                ivErr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListenerMine != null) {
                            mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                        }
                    }
                });
            }
        } else if (viewType == SEND_LOCATION || viewType == RECEIVE_LOCATION) {
            Map<String, Object> info = msgContent.getInfo();
            //TODO 这里崩溃过,问题如下,需要调试解决
            //Attempt to invoke virtual method 'void android.widget.TextView.setText(java.lang.CharSequence)' on a null object reference
            helper.setText(R.id.tvTitle, (String) info.get("name"));
            ImageView ivLocation = helper.getView(R.id.ivLocation);
            Glide.with(mContext).load(info.get("val")).placeholder(R.mipmap.default_location).centerCrop().into(ivLocation);

            ivLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListenerMine != null) {
                        mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                    }
                }
            });
            ivLocation.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    return mOnItemLongClickListenerMine != null ?
                            mOnItemLongClickListenerMine.onItemLongClick(helper, (ViewGroup) null, view, position) : false;
                }
            });

            ImageView ivErr = helper.getView(R.id.ivError);
            ivErr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListenerMine != null) {
                        mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                    }
                }
            });
        } else if (viewType == ADD_MEMBER) {
            Map<String, Object> info = msgContent.getInfo();
            String operation = info.get("act").toString();
            String operatorName = info.get("actName").toString();
            String targetUserDisplayNames = info.get("tgtName").toString();
            String tgtTopic = info.get("tgt").toString();
            int type = Integer.parseInt(info.get("type").toString());

            String me = Cache.getTinode().getMyId();
            TextView tv = helper.getView(R.id.tvNotification);
            SpannableString spStr = null;
            if (type == Drafty.MEMBER_OPERATE_TYPE_ADD) {
                if (operation.equals(me)) {
                    operatorName = UIUtils.getString(R.string.you);
                }

                if (tgtTopic.equals(me)) {
                    targetUserDisplayNames = UIUtils.getString(R.string.you);
                }
                spStr = new SpannableString(UIUtils.getString(R.string.invitation, operatorName, targetUserDisplayNames));
                if (!operation.equals(me)) {
                    NoLineClickSpan clickSpan = new NoLineClickSpan("#268F83", operation); //设置超链接
                    spStr.setSpan(clickSpan, 0, operatorName.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }

                if (!tgtTopic.equals(me)) {
                    NoLineClickSpan clickSpan = new NoLineClickSpan("#268F83", tgtTopic); //设置超链接
                    spStr.setSpan(clickSpan, operatorName.length() + 2, operatorName.length() + 2 + targetUserDisplayNames.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            } else if (type == Drafty.MEMBER_OPERATE_TYPE_REMOVE) {
                if (operation.equals(tgtTopic)) {
                    if (operation.equals(me)) {
                        operatorName = UIUtils.getString(R.string.you);
                    }

                    spStr = new SpannableString(UIUtils.getString(R.string.quit_groups, operatorName));
                    if (!operation.equals(me)) {
                        NoLineClickSpan clickSpan = new NoLineClickSpan("#268F83", tgtTopic); //设置超链接
                        spStr.setSpan(clickSpan, 0, operatorName.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    if (operation.equals(me)) {
                        operatorName = UIUtils.getString(R.string.you);
                    }

                    if (tgtTopic.equals(me)) {
                        targetUserDisplayNames = UIUtils.getString(R.string.you);
                    }
                    spStr = new SpannableString(UIUtils.getString(R.string.remove_self, targetUserDisplayNames, operatorName));

                    if (!tgtTopic.equals(me)) {
                        NoLineClickSpan clickSpan = new NoLineClickSpan("#268F83", tgtTopic); //设置超链接
                        spStr.setSpan(clickSpan, 0, targetUserDisplayNames.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                    if (!operation.equals(me)) {
                        NoLineClickSpan clickSpan = new NoLineClickSpan("#268F83", operation); //设置超链接
                        spStr.setSpan(clickSpan, targetUserDisplayNames.length() + 1, targetUserDisplayNames.length() + 1 + operatorName.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            if (spStr != null) {
                tv.setText(spStr);
            }
            //                if (operation.equalsIgnoreCase(GroupNotificationMessage.GROUP_OPERATION_CREATE)) {
            //                    notification = UIUtils.getString(R.string.created_group, operatorName);
            //                } else if (operation.equalsIgnoreCase(GroupNotificationMessage.GROUP_OPERATION_DISMISS)) {
            //                    notification = operatorName + UIUtils.getString(R.string.dismiss_groups);
            //                } else
            //                else if (operation.equalsIgnoreCase(GroupNotificationMessage.GROUP_OPERATION_RENAME)) {
            //                    notification = UIUtils.getString(R.string.change_group_name, operatorName, data.getTargetGroupName());
            //                }
        } else if (viewType == SEND_VOICE || viewType == RECEIVE_VOICE) {
            Drafty.Style[] fmt = msgContent.getStyles();

            if (fmt != null) {
                Drafty.Entity entity;
                Drafty.Style style = fmt[0];
                String tp = style.getType();
                entity = msgContent.getEntity(style);

                int length = Integer.parseInt(entity.getData().get("length").toString());
                int increment = (int) (UIUtils.getDisplayWidth() / 2 / AppConst.DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND * length);

                RelativeLayout rlAudio = helper.setText(R.id.tvDuration, length + "''").getView(R.id.rlAudio);
                ViewGroup.LayoutParams params = rlAudio.getLayoutParams();
                params.width = UIUtils.dip2Px(65) + UIUtils.dip2Px(increment);
                rlAudio.setLayoutParams(params);

                rlAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListenerMine != null) {
                            mOnItemClickListenerMine.onItemClick(helper, (ViewGroup) null, view, position);
                        }
                    }
                });
                rlAudio.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return mOnItemLongClickListenerMine != null ?
                                mOnItemLongClickListenerMine.onItemLongClick(helper, (ViewGroup) null, view, position) : false;
                    }
                });
            }
        } else if (viewType == SEND_RED_PACKET || viewType == RECEIVE_RED_PACKET) {
            //            RedPacketMessage redPacketMessage = (RedPacketMessage) msgContent;
            //            helper.setText(R.id.tvRedPacketGreeting, redPacketMessage.getContent());
        } else if (viewType == RECALL_NOTIFICATION) {
            String operatorName = "";
            if (item.isMine()) {
                operatorName = UIUtils.getString(R.string.you);
            } else {
                if (mPresenter.mConversationType == Topic.TopicType.P2P) {
                    operatorName = UIUtils.getString(R.string.other_party);
                } else {
                    Topic topic = Cache.getTinode().getTopic(mTopicName);
                    Subscription<VCard, ?> sub = topic != null ? topic.getSubscription(item.from) : null;
                    if (sub != null && sub.pub != null) {
                        VCard pub = sub.pub;
                        operatorName = pub.fn;
                    }
                }
            }
            helper.setText(R.id.tvNotification, UIUtils.getString(R.string.recall_one_message, operatorName));
        }
    }

    private void setOnClick(LQRViewHolderForRecyclerView helper, StoredMessage item, int position) {
        helper.getView(R.id.llError).setOnClickListener(v -> {
                }
        );
        helper.getView(R.id.ivAvatar).setOnClickListener(v -> {
            //            UserInfo userInfo = DBManager.getInstance().getUserInfo(item.getSenderUserId());
            //            if (userInfo != null) {
            //                Intent intent = new Intent(mContext, UserInfoActivity.class);
            //                intent.putExtra("userInfo", userInfo);
            //                ((SessionActivity) mContext).jumpToActivity(intent);
            //            }
        });
    }


    private void setStatus(LQRViewHolderForRecyclerView helper, StoredMessage item, int position, UploadAndGetUrl.FileInfo fileInfo) {
        int viewType = getItemViewType(item);
        Topic topic = Cache.getTinode().getTopic(mTopicName);
        StoredMessage m = item;

        if (viewType == SEND_TEXT || viewType == RECEIVE_TEXT || viewType == SEND_LOCATION || viewType == RECEIVE_LOCATION || viewType == SEND_VOICE || viewType == RECEIVE_VOICE) {
            //只需要设置自己发送的状态

            if(m.status == BaseDb.STATUS_ERROR)  helper.setViewVisibility(R.id.llError, View.VISIBLE);
            else helper.setViewVisibility(R.id.llError, View.GONE);

            if (m.seq <= 0) {
                helper.setViewVisibility(R.id.pbSending, View.VISIBLE).setViewVisibility(R.id.llError, View.GONE);
            } else if (topic != null) {
                Log.d(TAG,"R.id.pbSending viewId="+R.id.pbSending);
                Log.d(TAG,"item="+new Gson().toJson(item.content));
                Log.d(TAG,"item position="+position);

                //LQRViewHolder holder1=helper.setViewVisibility(R.id.pbSending, View.GONE);
                //holder1.setViewVisibility(R.id.llError, View.GONE);
                if (topic.msgReadCount(m.seq) > 0) {//已阅读
                    //                    holder.mDeliveredIcon.setImageResource(R.drawable.ic_visibility);
                } else if (topic.msgRecvCount(m.seq) > 0) {
                    //                    已接收
                    //                    holder.mDeliveredIcon.setImageResource(R.drawable.ic_done_all);
                } else {
                    //                    已发送
                }
            }
        } else if (viewType == SEND_IMAGE || viewType == RECEIVE_IMAGE) {
            BubbleImageView bivPic = helper.getView(R.id.bivPic);
            if(bivPic==null){
                return;
            }

            if(m.status == BaseDb.STATUS_ERROR)  helper.setViewVisibility(R.id.llError, View.VISIBLE);
            else helper.setViewVisibility(R.id.llError, View.GONE);

            if (m.seq <= 0||m.status<= BaseDb.STATUS_VISIBLE) {
                //<0表示上传文件，>0表示下载文件
                bivPic.showShadow(true);
                if (fileInfo != null) {
                    bivPic.setProgressVisible(true);
                    bivPic.setPercent(fileInfo.progress);
                } else {
                    bivPic.setProgressVisible(false);
                }

                String path = (String) item.content.ent[0].data.get("local");
                LogUtils.w("seq <0 img path setstatus" + path);
                if (path != null) {
                    Glide.with(mContext).load(path).error(R.mipmap.default_img_failed)
                            .override(UIUtils.dip2Px(150), UIUtils.dip2Px(300))
                            .fitCenter()
                            .into(bivPic);
                }
            } else if (topic != null) {
                if (fileInfo != null) {
                    bivPic.setProgressVisible(true);
                    bivPic.showShadow(true);
                    bivPic.setPercent(fileInfo.progress);
                } else {
                    bivPic.setProgressVisible(false);
                    bivPic.showShadow(false);

                    if (topic.msgReadCount(m.seq) > 0) {//已阅读
                        //                    holder.mDeliveredIcon.setImageResource(R.drawable.ic_visibility);
                    } else if (topic.msgRecvCount(m.seq) > 0) {
                        //                    已接收
                        //                    holder.mDeliveredIcon.setImageResource(R.drawable.ic_done_all);
                    } else {
                        //                    已发送
                    }
                }
            } else {
                bivPic.setProgressVisible(false);
                bivPic.showShadow(false);
            }
        } else if (viewType == SEND_FILE || viewType == RECEIVE_FILE) {
            if(m.status == BaseDb.STATUS_ERROR)  helper.setViewVisibility(R.id.llError, View.VISIBLE);
            else helper.setViewVisibility(R.id.llError, View.GONE);

            if (m.seq <= 0) {
                //表示上传文件，>0表示下载文件
                helper.setViewVisibility(R.id.pbSending, View.VISIBLE);
                if (fileInfo != null) {
                    ((NumberProgressBar) helper.getView(R.id.pbSending)).setReachedBarColor(Color.BLUE);
                    ((NumberProgressBar) helper.getView(R.id.pbSending)).setProgress(fileInfo.progress);
                }
            } else if (topic != null) {
                if (fileInfo != null) {
                    helper.setViewVisibility(R.id.pbSending, View.VISIBLE);
                    ((NumberProgressBar) helper.getView(R.id.pbSending)).setReachedBarColor(Color.RED);
                    ((NumberProgressBar) helper.getView(R.id.pbSending)).setProgress(fileInfo.progress);
                } else {
                    helper.setViewVisibility(R.id.pbSending, View.GONE);
                    if (topic.msgReadCount(m.seq) > 0) {//已阅读
                        //                    holder.mDeliveredIcon.setImageResource(R.drawable.ic_visibility);
                    } else if (topic.msgRecvCount(m.seq) > 0) {
                        //                    已接收
                        //                    holder.mDeliveredIcon.setImageResource(R.drawable.ic_done_all);
                    } else {
                        //                    已发送
                    }
                }
            }
            //            viewType == SEND_STICKER ||viewType == RECEIVE_STICKER ||
        } else if (viewType == SEND_VIDEO || viewType == RECEIVE_VIDEO) {
            BubbleImageView bivPic = helper.getView(R.id.bivPic);
            if(bivPic==null){
                return;
            }
            CircularProgressBar cpbLoading = helper.getView(R.id.cpbLoading);

            if(m.status == BaseDb.STATUS_ERROR)  helper.setViewVisibility(R.id.llError, View.VISIBLE);
            else helper.setViewVisibility(R.id.llError, View.GONE);

            if (m.seq <= 0||m.status<= BaseDb.STATUS_VISIBLE) {
                //表示上传文件，>0表示下载文件
                bivPic.showShadow(true);
                if (fileInfo != null) {
                    if(cpbLoading.getProgress() == -1) cpbLoading.setVisibility(View.INVISIBLE);
                    else cpbLoading.setVisibility(View.VISIBLE);
                    cpbLoading.setBackgroundColor(Color.parseColor("#6DCAEC"));
                    cpbLoading.setProgress(fileInfo.progress);
                } else {
                    cpbLoading.setVisibility(View.GONE);
                }
            } else if (topic != null) {
                if (fileInfo != null) {
                    if(cpbLoading.getProgress() == -1) cpbLoading.setVisibility(View.INVISIBLE);
                    else cpbLoading.setVisibility(View.VISIBLE);
                    bivPic.showShadow(true);

                    cpbLoading.setBackgroundColor(Color.RED);
                    cpbLoading.setProgress(fileInfo.progress);
                } else {
                    bivPic.showShadow(false);
                    cpbLoading.setVisibility(View.GONE);
                    if (topic.msgReadCount(m.seq) > 0) {//已阅读
                        //                    holder.mDeliveredIcon.setImageResource(R.drawable.ic_visibility);
                    } else if (topic.msgRecvCount(m.seq) > 0) {
                        //                    已接收
                        //                    holder.mDeliveredIcon.setImageResource(R.drawable.ic_done_all);
                    } else {
                        //                    已发送
                    }
                }
            }
        } else if (viewType == SEND_IMAGE_INLINE || viewType == RECEIVE_IMAGE_INLINE) {
            BubbleImageView bivPic = helper.getView(R.id.bivPic);
            if(bivPic==null){
                return;
            }
            bivPic.setProgressVisible(false);
            bivPic.showShadow(false);
        } else {
            helper.setViewVisibility(R.id.pbSending, View.GONE).setViewVisibility(R.id.llError, View.GONE);
        }
    }

    private void  setAvatar(LQRViewHolderForRecyclerView helper, StoredMessage item, int position) {
        ImageView ivAvatar = helper.getView(R.id.ivAvatar);
        String myUid = Cache.getTinode().getMyId();
        if (item.from.equals(myUid)) {
            MeTopic meTopic = Cache.getTinode().getMeTopic();
            VCard mePub = (VCard) meTopic.getPub();
            if (mePub == null) {
                return;
            }
            UiUtils.assignBitmap(mContext, ivAvatar,
                    mePub != null ? mePub.getBitmap() : null,
                    mePub != null ? mePub.fn : null,
                    meTopic.getName());

            ((TextView) helper.getView(R.id.tvName)).setText(mePub != null ? mePub.fn : "");
        } else {
            Topic topic = Cache.getTinode().getTopic(mTopicName);
            if (topic == null) {
                return;
            }
            // 获取当前 group 的人的列表
            Collection<Subscription> subscriptions = topic.getSubscriptions();
            if (subscriptions == null) {
                //暂时容错
                return;
            }
            VCard userInfo = new VCard();
            for (Subscription sub : subscriptions) {
                if (sub.user.equals(item.from)) {
                    userInfo = (VCard) sub.pub;
                    break;
                }
            }

            UiUtils.assignBitmap(mContext, ivAvatar,
                    userInfo != null ? userInfo.getBitmap() : null,
                    userInfo != null ? userInfo.fn : null,
                    topic.getName());
            ((TextView) helper.getView(R.id.tvName)).setText(userInfo != null ? userInfo.fn : "");
        }
    }

    private void setTime(LQRViewHolderForRecyclerView helper, StoredMessage item, int position) {
        long msgTime = item.ts.getTime();
        if (position > 0) {
            StoredMessage preMsg = null;
            if (mData.size() > position) {
                preMsg = mData.get(position - 1);
            }

            if (preMsg == null) {
                preMsg = getMessage(position - 1);
            }

            long preMsgTime = preMsg.ts.getTime();
            if (msgTime - preMsgTime > (5 * 60 * 1000)) {
                helper.setViewVisibility(R.id.tvTime, View.VISIBLE).setText(R.id.tvTime, TimeUtils.getMsgFormatTime(msgTime, true));
            } else {
                helper.setViewVisibility(R.id.tvTime, View.GONE);
            }
        } else {
            helper.setViewVisibility(R.id.tvTime, View.VISIBLE).setText(R.id.tvTime, TimeUtils.getMsgFormatTime(msgTime, true));
        }
    }

    public StoredMessage getMessage(int position) {
        mCursor.moveToPosition(mCursor.getCount() - position - 1);

        StoredMessage msg = StoredMessage.readMessage(mCursor);

        if (mData.size() > position) {
            mData.set(position, msg);
        } else {
            int toAdd = position - mData.size();
            BuglyLog.d(TAG, "getMessage add null num="+toAdd);
            for (int index = 0; index < toAdd; ++index) {
                mData.add(null);
            }
            mData.add(position, msg);
        }
        return msg;
    }

    public static String getMsgText(StoredMessage msg, String defaultStr) {
        String msgText = defaultStr;
        int viewType = getItemViewType(msg);
        if (viewType == SEND_TEXT || viewType == RECEIVE_TEXT) {
            msgText = msg.content.txt;
        } else if (viewType == SEND_LOCATION || viewType == RECEIVE_LOCATION) {
            msgText = "[位置]";
        } else if (viewType == SEND_VOICE || viewType == RECEIVE_VOICE) {
            msgText = "[语音]";
        } else if (viewType == SEND_IMAGE || viewType == RECEIVE_IMAGE || viewType == SEND_IMAGE_INLINE || viewType == RECEIVE_IMAGE_INLINE) {
            msgText = "[图片]";
        } else if (viewType == SEND_FILE || viewType == RECEIVE_FILE) {
            msgText = "[文件]";
        } else if (viewType == SEND_VIDEO || viewType == RECEIVE_VIDEO) {
            msgText = "[视频]";
        } else {
        }
        return msgText;
    }

    //获取字符串类型的消息
    public static String getMsgText(StoredMessage msg) {
        return getMsgText(msg, "收到一条消息");
    }

    public static int getItemViewType(StoredMessage msg) {
        boolean isSend = msg.isMine();

        Drafty content = msg.content;
        int viewType = UNDEFINE_MSG;
        if (content == null) {
            // Malicious user may send a message with null content.
            return viewType;
        }

        Drafty.Style[] fmt = content.getStyles();
        SpannableStringBuilder text = new SpannableStringBuilder(content.toString());

        if (fmt != null) {
            Drafty.Entity entity;

            //            for (Drafty.Style style : fmt) {
            //                CharacterStyle span = null;
            //                int offset = -1, length = -1;
            Drafty.Style style = fmt[0];
            String tp = style.getType();
            entity = content.getEntity(style);

            final Map<String, Object> data;
            if (entity != null) {
                tp = entity.getType();
                data = entity.getData();
            } else {
                data = null;
            }

            if (tp == null) {
                Log.d(TAG, "Null type in " + style.toString());
                //                continue;
            }

            viewType = isSend ? SEND_TEXT : RECEIVE_TEXT;
            switch (tp) {
                case "ST":
                    //                    span = new StyleSpan(Typeface.BOLD);
                    break;
                case "EM":
                    //                    span = new StyleSpan(Typeface.ITALIC);
                    break;
                case "DL":
                    //                    span = new StrikethroughSpan();
                    break;
                case "CO":
                    //                    span = new TypefaceSpan("monospace");
                    break;
                case "BR":
                    text.replace(style.getOffset(), style.getOffset() + style.length(), "\n");
                    //                    span = null;
                    break;
                case "LN":
                    //                    String url = data != null ? (String) data.get("url") : null;
                    //                    span = url != null ? new URLSpan(url) {
                    //                        @Override
                    //                        public void onClick(View widget) {
                    //								if (clicker != null) {
                    //									clicker.onClick("LN", data);
                    //								}
                    //                        }
                    //                    } : null;
                    break;
                case "MN":
                    //                    span = null;
                    break;
                case "HT":
                    //                    span = null;
                    break;
                case "IM":
                    viewType = isSend ? SEND_IMAGE_INLINE : RECEIVE_IMAGE_INLINE;
                    break;
                case "IMAGE":
                    //image
                    if (data.get("val") == null) {
                        data.put("val", data.get("ref"));
                    }
                    viewType = isSend ? SEND_IMAGE : RECEIVE_IMAGE;
                    break;
                case "VC":
                    //voice
                    viewType = isSend ? SEND_VOICE : RECEIVE_VOICE;
                    break;
                case "STICKER":
                    viewType = isSend ? SEND_STICKER : RECEIVE_STICKER;
                    break;
                case "EX":
                    //file
                    if (MediaFileUtils.isImageFileType(data.get("name").toString())) {
//                        if (data.get("val") != null) {
//                            Log.d(TAG,"Ex but is image and val!=null");
//                            viewType = isSend ? SEND_IMAGE_INLINE : RECEIVE_IMAGE_INLINE;
//                        } else {
                            //val本来表示文件内容，使用ref字段填充
                        Log.e(TAG, "Ex but is image and content：" + new Gson().toJson(data));
                            Log.d(TAG,"Ex but is image and val==null");
                            data.put("val", data.get("ref"));
                            viewType = isSend ? SEND_IMAGE : RECEIVE_IMAGE;
                       // }
                    } else if (MediaFileUtils.isVideoFileType(data.get("name").toString())) {
                        Log.d(TAG,"Ex but is video!");
                        viewType = isSend ? SEND_VIDEO : RECEIVE_VIDEO;
                    } else {
                        Log.d(TAG,"Ex and is file!");
                        viewType = isSend ? SEND_FILE : RECEIVE_FILE;
                    }
                    break;
                case "LOC":
                    viewType = isSend ? SEND_LOCATION : RECEIVE_LOCATION;
                    break;
                case "NOTIFY":
                    viewType = RECEIVE_NOTIFICATION;
                    break;
                case "RED":
                    viewType = isSend ? SEND_RED_PACKET : RECEIVE_RED_PACKET;
                    break;
                case "RECALL":
                    viewType = RECALL_NOTIFICATION;
                    break;
                case "MEMBER":
                    viewType = ADD_MEMBER;
                    break;
                default:
                    // TODO(gene): report unknown style to user
                    break;
            }
            //        }
        } else {
            if (viewType == UNDEFINE_MSG && content.txt != null) {
                viewType = isSend ? SEND_TEXT : RECEIVE_TEXT;
            }
        }
        return viewType;
    }

    @Override
    public int getItemViewType(int position) {
        StoredMessage msg = null;
        if (mData.size() > position) {
            msg = mData.get(position);
        }

        if (msg == null) {
            msg = getMessage(position);
        }
        //		Topic.TopicType tp = Topic.getTopicTypeByName(mTopicName);
        int type = getItemViewType(msg);
        return type;
    }

    //////tinode
    private static final int MESSAGES_TO_LOAD = 20;
    private static final int MESSAGES_QUERY_ID = 100;
    private MessageLoaderCallbacks mLoaderCallbacks;
    private Cursor mCursor;
    private String mTopicName;
    private int mPagesToLoad;
    private boolean scrollToBottom = false;

    public void runLoader(boolean scrollToBottom) {
        //发送数据后，数据集合关系就改变了
        mData.clear();

        this.scrollToBottom = scrollToBottom;
        LoaderManager lm = LoaderManager.getInstance(mContext);
        //        if (lm.hasRunningLoaders()) {
        //            return;
        //        }
        final Loader<Cursor> loader = lm.getLoader(MESSAGES_QUERY_ID);
        if (loader != null && !loader.isReset()) {
            lm.restartLoader(MESSAGES_QUERY_ID, null, mLoaderCallbacks);
        } else {
            lm.initLoader(MESSAGES_QUERY_ID, null, mLoaderCallbacks);
        }
    }

    public void runLoader() {
        runLoader(false);
    }

    public void setTopicName(final String topicName) {
        mTopicName = topicName;
    }

    public void swapCursor(final String topicName, final Cursor cursor) {
        if (mCursor != null && mCursor == cursor) {
            return;
        }
        // Clear selection
        //        if (mSelectionMode != null) {
        //            mSelectionMode.finish();
        //            mSelectionMode = null;
        //        }
        mTopicName = topicName;
        Cursor oldCursor = mCursor;
        mCursor = cursor;
        if (oldCursor != null) {
            oldCursor.close();
        }
        // Log.d(TAG, "swapped cursor, topic=" + mTopicName);
    }

    public boolean loadNextPage() {
        if (getItemCount() == mPagesToLoad * MESSAGES_TO_LOAD) {
            mPagesToLoad++;
            runLoader();
            return true;
        }

        return false;
    }

    private class MessageLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == MESSAGES_QUERY_ID) {
                return new MessageDb.Loader(mContext, mTopicName, mPagesToLoad, MESSAGES_TO_LOAD);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (loader.getId() == MESSAGES_QUERY_ID) {
                swapCursor(mTopicName, cursor);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (loader.getId() == MESSAGES_QUERY_ID) {
                swapCursor(null, null);
            }
        }

        private void swapCursor(final String topicName, final Cursor cursor) {
            SessionAdapter.this.swapCursor(topicName, cursor);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BGARefreshLayout mRefresher = SessionAdapter.this.mPresenter.getRefreshLayout();
                    mRefresher.endRefreshing();
                    notifyDataSetChanged();
                    //                    发送消息时需要滚动到最后一条；加载更多消息时不需要
                    if (scrollToBottom && cursor != null)
                        mRecyclerView.scrollToPosition(cursor.getCount() - 1);

                    SessionAdapter.this.mPresenter.searchMsgToInSight();
                }
            });
        }

    }

    private OnItemClickListener mOnItemClickListenerMine;
    private OnItemLongClickListener mOnItemLongClickListenerMine;

    public void setOnItemClickListenerMine(OnItemClickListener onItemClickListener) {
        mOnItemClickListenerMine = onItemClickListener;
    }

    public void setOnItemLongClickListenerMine(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListenerMine = onItemLongClickListener;
    }


    // This is a map of message IDs to their corresponding loader IDs.
    // This is needed for upload cancellations.
    private LongSparseArray<Integer> mLoaders;

    /////send file by tinode
    public void addLoaderMapping(Long msgId, int loaderId) {
        mLoaders.put(msgId, loaderId);
    }

    public Integer getLoaderMapping(Long msgId) {
        return mLoaders.get(msgId);
    }

    private boolean cancelUpload(long msgId) {
        Integer loaderId = mLoaders.get(msgId);
        if (loaderId != null) {
            LoaderManager.getInstance(mContext).destroyLoader(loaderId);
            // Change mapping to force background loading process to return early.
            addLoaderMapping(msgId, -1);
            return true;
        }
        return false;
    }

}
