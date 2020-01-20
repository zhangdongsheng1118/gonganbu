package com.zuozhan.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.starrtc.demo.demo.MLOC;
import com.starrtc.demo.demo.audiolive.AudioLiveActivity;
import com.starrtc.demo.demo.superroom.SuperRoomActivity;
import com.starrtc.demo.demo.superroom.SuperRoomCreateActivity;
import com.starrtc.starrtcsdk.api.XHConstants;
import com.yixin.tinode.R;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.activity.ZHRenwuInfoActivity;
import com.zuozhan.app.bean.MyUtil;
import com.zuozhan.app.bean.RenWuLeiBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RenWuAdapter extends RecyclerView.Adapter<RenWuAdapter.ViewHolder> {
    private  Context mContext;
    ArrayList<RenWuLeiBean.DataBean> arrayList = new ArrayList<>();

    public void reshes(List<RenWuLeiBean.DataBean> dataBeans) {
        this.arrayList.clear();
        if (dataBeans != null){
            this.arrayList.addAll(dataBeans);
        }
        notifyDataSetChanged();
    }
    public RenWuAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zh_item_rw_two, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        MyUtil.setText(viewHolder.anjianmengcheng,arrayList.get(i).caseName,"案件名称：");
        MyUtil.setText(viewHolder.renwumingcheng,arrayList.get(i).name,"任务名称：");
        MyUtil.setText(viewHolder.duixiang,arrayList.get(i).actionObject,"行动对象：");
        MyUtil.setText(viewHolder.fzeren,arrayList.get(i).responsibleUserName,"负责人：");
        MyUtil.setText(viewHolder.renwumiaoshu,arrayList.get(i).getDescription());
        viewHolder.shijian.setText("发布时间：" + arrayList.get(i).createTime);
        if (0 == arrayList.get(i).getStatus()) {
            viewHolder.jieshou.setText("待接受");
            viewHolder.jieshou.setTextColor(Color.parseColor("#D3A36A"));
        } else if (1 == arrayList.get(i).getStatus()) {
            viewHolder.jieshou.setText("进行中");
            viewHolder.jieshou.setTextColor(Color.parseColor("#D36A6A"));
        }  else if (2 == arrayList.get(i).getStatus()) {
            viewHolder.jieshou.setText("已结束");
            viewHolder.jieshou.setTextColor(Color.parseColor("#6AD3C0"));
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(mContext, ZHRenwuInfoActivity.class);
//                intent.putExtra(AudioLiveActivity.LIVE_TYPE, XHConstants.XHSuperRoomType.XHSuperRoomTypeGlobalPublic);
//                intent.putExtra(AudioLiveActivity.LIVE_NAME,"800");
//                intent.putExtra(AudioLiveActivity.LIVE_ID,"Wz@NWuVjdJZgaA4Ba4amSHRDcWvb8kzL");
//                intent.putExtra(AudioLiveActivity.CREATER_ID, "8");
//                intent.putExtra("info",(Serializable) arrayList.get(i));
//                mContext.startActivity(intent);
                RouterUtil.goActivity((Activity) mContext,ZHRenwuInfoActivity.class, (Serializable) arrayList.get(i),arrayList.get(i).getStatus()+"");
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView anjianmengcheng;
        TextView renwumingcheng;
        TextView duixiang;
        TextView fzeren;
        TextView renwumiaoshu;
        TextView shijian;
        TextView jieshou;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            anjianmengcheng = itemView.findViewById(R.id.ajmengcheng);
            renwumingcheng = itemView.findViewById(R.id.rwmingcheng);
            duixiang = itemView.findViewById(R.id.duixiang);
            fzeren = itemView.findViewById(R.id.fuzeren);
            renwumiaoshu = itemView.findViewById(R.id.rwmiaohu);
            shijian = itemView.findViewById(R.id.date_time);
            jieshou = itemView.findViewById(R.id.js);
        }
    }

}
