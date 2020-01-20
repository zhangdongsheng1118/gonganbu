package com.zuozhan.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.bean.ArticleBean;
import com.zuozhan.app.bean.JiaoLiuBean;
import com.zuozhan.app.bean.MyUtil;

import java.util.ArrayList;
import java.util.List;

public class TongZhiAdapter extends RecyclerView.Adapter<TongZhiAdapter.ViewHolder> {
    private Context mContext;
    ArrayList<ArticleBean.DataBean> arrayList = new ArrayList<>();

    public TongZhiAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void reshes(List<ArticleBean.DataBean> arrayList) {
        this.arrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zh_item_tongzhi, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        MyUtil.setText(viewHolder.tong, arrayList.get(i).title);
        MyUtil.setText(viewHolder.mess, arrayList.get(i).summary);
        viewHolder.timme.setText("通告时间：" + arrayList.get(i).createTime);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.goWebView((Activity) mContext, arrayList.get(i).title, arrayList.get(i).article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tong;
        TextView mess;
        TextView timme;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tong = itemView.findViewById(R.id.jitg);
            mess = itemView.findViewById(R.id.tgnr);
            timme = itemView.findViewById(R.id.tgsj);
        }
    }
}
