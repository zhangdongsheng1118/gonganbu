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
import com.zuozhan.app.bean.MyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleRecyclerAdapter extends RecyclerView.Adapter<ArticleRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<ArticleBean.DataBean> reportBeans=new ArrayList<>();

    public ArticleRecyclerAdapter(Context mContext, List<ArticleBean.DataBean> reportBeans) {
        this.mContext = mContext;
        this.reportBeans = reportBeans;
        if (reportBeans != null) {
            this.reportBeans = reportBeans;
        }
    }

    public void setData(List<ArticleBean.DataBean> messageBeans) {
        if (messageBeans != null) {
            this.reportBeans.clear();
            this.reportBeans.addAll(messageBeans);
        } else {
            this.reportBeans.clear();
        }
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view =
                LayoutInflater.from(mContext).inflate(R.layout.zh_item_caozuo, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        ArticleBean.DataBean dataBean = reportBeans.get(i);
        MyUtil.setText(viewHolder.caozuo_text1,dataBean.title);
        MyUtil.setText(viewHolder.caozuo_text2,dataBean.createTime);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.goWebView((Activity) mContext,dataBean.title,dataBean.article);
            }
        });
    }

    Map<Integer, Integer> maps = new HashMap<>();

    @Override
    public int getItemCount() {
        return reportBeans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView caozuo_text1;
        TextView caozuo_text2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            caozuo_text1 = itemView.findViewById(R.id.caozuo_text1);
            caozuo_text2 = itemView.findViewById(R.id.caozuo_text2);
        }
    }
}
