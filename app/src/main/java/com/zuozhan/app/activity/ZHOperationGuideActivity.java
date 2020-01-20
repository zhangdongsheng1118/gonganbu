package com.zuozhan.app.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.zuozhan.app.adapter.ArticleRecyclerAdapter;
import com.zuozhan.app.bean.ArticleBean;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.httpUtils.MyService;
import com.zuozhan.app.net.HttpService;
import com.zuozhan.app.treelist.Node;
import com.zuozhan.app.treelist.OnTreeNodeClickListener;
import com.zuozhan.app.treelist.SimpleTreeRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZHOperationGuideActivity extends AllBaseActivity {
    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;
    @BindView(R.id.recyle_right)
    RecyclerView recyle_right;
    @BindView(R.id.title_left)
    View title_left;
    @BindView(R.id.title_center)
    TextView title_center;
    @BindView(R.id.drawlayout)
    DrawerLayout drawlayout;
    @BindView(R.id.main_right_drawer_layout)
    View main_right_drawer_layout;
    @BindView(R.id.diandian)
    View diandian;
    ArticleRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_caozuozhinan);
        title_center.setText("操作指南");
        title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(manager);
        adapter = new ArticleRecyclerAdapter(this,new ArrayList<>());
        recycler_view.setAdapter(adapter);

        HttpService.getApi(MyService.class)
                .getServiceApi().getArticleByTypeByType()
                .enqueue(new Callback<ArticleBean>() {
                    @Override
                    public void onResponse(Call<ArticleBean> call, Response<ArticleBean> response) {
                        ArticleBean articleBean =response.body();
                        if (articleBean==null){
                            return;
                        }
                        adapter.setData(articleBean.data);
                    }

                    @Override
                    public void onFailure(Call<ArticleBean> call, Throwable t) {

                    }
                });


        drawlayout.setScrimColor(Color.TRANSPARENT);
        drawlayout.setDrawerListener(new ActionBarDrawerToggle(this, drawlayout, R.mipmap.ic_launcher,
                R.string.open, R.string.close) {
            //菜单打开
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            // 菜单关闭
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        });
        diandian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRightLayout();
            }
        });
        initDrawer();
    }


    SimpleTreeRecyclerAdapter mAdapter;
    private void initDrawer(){
        recyle_right.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SimpleTreeRecyclerAdapter(recyle_right, this,
                new ArrayList<>(), 1,R.drawable.shipingziyuan_xiala_icon,R.drawable.xiugaixinxi_shouqi_icon);

        recyle_right.setAdapter(mAdapter);
        mAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
            @Override
            public void onClick(Node node, int position) {
                if (node==null){
                    return;
                }
                if (!node.isLeaf()){
                   return;
                }
                HttpService.getApi(MyService.class)
                        .getServiceApi().getArticle(node.other)
                        .enqueue(new Callback<ArticleBean>() {
                            @Override
                            public void onResponse(Call<ArticleBean> call, Response<ArticleBean> response) {
                                ArticleBean articleBean =response.body();
                                if (articleBean==null){
                                    return;
                                }
                                adapter.setData(articleBean.data);
                                openRightLayout();
                            }

                            @Override
                            public void onFailure(Call<ArticleBean> call, Throwable t) {

                            }
                        });
            }
        });
        HttpUtil.getType1(new HttpUtil.Callback<ArticleBean>() {
            @Override
            public void onResponse(ArticleBean call) {
                if (call!=null && call.data!=null){
                    List<Node> mDatas=new ArrayList<>();
                    for (int i = 0; i < call.data.size(); i++) {
                        mDatas.add(new Node(call.data.get(i).id, -1, call.data.get(i).name,call.data.get(i).id+"",null));
                    }
                    mAdapter.addData(mDatas);
                }
            }
        });
    }


    // 右边菜单开关事件
    public void openRightLayout() {
        if (drawlayout.isDrawerOpen(main_right_drawer_layout)) {
            drawlayout.closeDrawer(main_right_drawer_layout);
        } else {
            drawlayout.openDrawer(main_right_drawer_layout);
        }
    }
}
