package com.zuozhan.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.activity.ZHExceptionInfoActivity;
import com.zuozhan.app.bean.EeceptionBean;
import com.zuozhan.app.bean.MyUtil;
import com.zuozhan.app.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class ExceptionListAdapter extends RecyclerView.Adapter<ExceptionListAdapter.ViewHolder> {
    private Context mContext;
    ArrayList<EeceptionBean.DataBean> arrayList = new ArrayList<>();

    public ExceptionListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void reshes(List<EeceptionBean.DataBean> arrayList) {
        this.arrayList.clear();
        if (arrayList != null) {
            this.arrayList.addAll(arrayList);
            notifyDataSetChanged();
        } else {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zh_item_yichanglei, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        MyUtil.setText(viewHolder.item_tv1, arrayList.get(i).name, "异常名称：");
        MyUtil.setText(viewHolder.item_tv3, arrayList.get(i).createUserName, "提交人：");
        MyUtil.setText(viewHolder.item_tv4, arrayList.get(i).content);
        try {
            MyUtil.setText(viewHolder.item_tv5, DateUtil.timeStamp3Date(arrayList.get(i).createTime + ""), "提交时间：");
        } catch (Exception e) {
            MyUtil.setText(viewHolder.item_tv5, arrayList.get(i).createTime, "提交时间：");
        }

        if (arrayList.get(i).status == 0) {
            MyUtil.setText(viewHolder.item_tv2, "未知状态" + arrayList.get(i).status);
            viewHolder.item_tv2.setTextColor(Color.parseColor("#D3A36A"));
        } else if (arrayList.get(i).status == 1) {
            MyUtil.setText(viewHolder.item_tv2, "进行中");
            viewHolder.item_tv2.setTextColor(Color.parseColor("#D36A6A"));
        } else if (arrayList.get(i).status == 2) {
            MyUtil.setText(viewHolder.item_tv2, "解除");
            viewHolder.item_tv2.setTextColor(Color.parseColor("#6AD3C0"));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.goActivity((Activity) mContext, ZHExceptionInfoActivity.class, arrayList.get(i));
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_tv1;
        TextView item_tv2;
        TextView item_tv3;
        TextView item_tv4;
        TextView item_tv5;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_tv1 = itemView.findViewById(R.id.item_tv1);
            item_tv2 = itemView.findViewById(R.id.item_tv2);
            item_tv3 = itemView.findViewById(R.id.item_tv3);
            item_tv4 = itemView.findViewById(R.id.item_tv4);
            item_tv5 = itemView.findViewById(R.id.item_tv5);
        }
    }
}
