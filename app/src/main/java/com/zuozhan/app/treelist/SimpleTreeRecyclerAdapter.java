package com.zuozhan.app.treelist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.zuozhan.app.imageloader.ImageLoader;
import com.zuozhan.app.util.ToastUtils;

import java.util.List;

/**
 * Created by zhangke on 2017-1-14.
 */
public class SimpleTreeRecyclerAdapter extends TreeRecyclerAdapter {

    public SimpleTreeRecyclerAdapter(RecyclerView mTree, Context context, List<Node> datas, int defaultExpandLevel, int iconExpand, int iconNoExpand) {
        super(mTree, context, datas, defaultExpandLevel, iconExpand, iconNoExpand);
    }

    public SimpleTreeRecyclerAdapter(RecyclerView mTree, Context context, List<Node> datas, int defaultExpandLevel) {
        super(mTree, context, datas, defaultExpandLevel);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHoder(View.inflate(mContext, R.layout.zh_tree_list_item, null));
    }

    @Override
    public void onBindViewHolder(final Node node, RecyclerView.ViewHolder holder, int position) {

        final MyHoder viewHolder = (MyHoder) holder;
        if (node.getIcon() == -1) {
            viewHolder.icon.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.icon.setVisibility(View.VISIBLE);
//            viewHolder.icon.setImageResource(node.getIcon());
            ImageLoader.loadImage(mContext,viewHolder.icon,node.getIcon());
        }
        viewHolder.label.setText(node.getName());
//        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ToastUtils.showToast(""+node.getName());
//            }
//        });
    }

    class MyHoder extends RecyclerView.ViewHolder {

        public TextView label;

        public ImageView icon;

        public MyHoder(View itemView) {
            super(itemView);
            label = (TextView) itemView
                    .findViewById(R.id.id_treenode_label);
            icon = (ImageView) itemView.findViewById(R.id.icon);

        }

    }
}
