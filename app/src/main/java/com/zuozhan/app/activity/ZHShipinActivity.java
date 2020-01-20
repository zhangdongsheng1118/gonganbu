package com.zuozhan.app.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.bean.CarBean;
import com.zuozhan.app.bean.DeviceBean;
import com.zuozhan.app.bean.MyUtil;
import com.zuozhan.app.bean.RenWuLeiBean;
import com.zuozhan.app.bean.UBean;
import com.zuozhan.app.bean.VIdeoListBean;
import com.zuozhan.app.bean.VideoSourceBean;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.imageloader.ImageLoader;
import com.zuozhan.app.treelist.Node;
import com.zuozhan.app.treelist.OnTreeNodeClickListener;
import com.zuozhan.app.treelist.SimpleTreeRecyclerAdapter;
import com.zuozhan.app.util.DateUtil;
import com.zuozhan.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ZHShipinActivity extends AllBaseActivity {

    @BindView(R.id.title_left)
    ImageView imageView;

    @BindView(R.id.recyle_right0)
    RecyclerView recyle_right0;
    @BindView(R.id.recyle_right)
    RecyclerView recyle_right;
    @BindView(R.id.drawlayout)
    DrawerLayout drawlayout;
    @BindView(R.id.main_right_drawer_layout)
    View main_right_drawer_layout;
    @BindView(R.id.diandian)
    View diandian;
    @BindView(R.id.video_1)
    View video_1;
    @BindView(R.id.video_2)
    View video_2;
    @BindView(R.id.video1_iv)
    ImageView video1_iv;
    @BindView(R.id.video2_iv)
    ImageView video2_iv;
    @BindView(R.id.video1_tv)
    TextView video1_tv;
    @BindView(R.id.video2_tv)
    TextView video2_tv;
    @BindView(R.id.timer_edit)
    TextView timer_edit;
    @BindView(R.id.group_bottom)
    RadioGroup group_bottom;

    String startTime, endTime, devId, userId, carId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_shi_pin_zi_yuan);

        //点击返回箭头关闭此页面
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        diandian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRightLayout();
            }
        });
        timer_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSoft();
                    //点击搜索的时候隐藏软键盘
                    String str = timer_edit.getText().toString();
                    if (!str.contains("-")) {
                        ToastUtils.showToast("请输入正确的日期格式例：2019/06/20-2019/06/21");
                        return true;
                    }
                    String s[] = str.split("-");
                    if (s.length != 2) {
                        ToastUtils.showToast("请输入正确的日期格式例：2019/06/20-2019/06/21");
                        return true;
                    }
                    String s1 = DateUtil.timeStampDay(s[0]);
                    String s2 = DateUtil.timeStampDay(s[1]);
                    if (TextUtils.isEmpty(s1) || TextUtils.isEmpty(s2)) {
                        ToastUtils.showToast("请输入正确的日期格式例：2019/06/20-2019/06/21");
                        return true;
                    }
                    startTime = s1;
                    endTime = s2;
                    getData();
                    // 在这里写搜索的操作,一般都是网络请求数据
                    return true;
                }
                return false;
            }
        });

        initDrawer();
        getData();
    }

    SimpleTreeRecyclerAdapter mAdapter0;
    SimpleTreeRecyclerAdapter mAdapter;
    VIdeoListBean vIdeoListBean;

    private void initDrawer() {
        List<Node> datas0 = new ArrayList<>();
        datas0.add(new Node("1", "0", "时间", "1"));
        datas0.add(new Node("2", "1", "最近三天", "2"));
        datas0.add(new Node("3", "1", "最近一周", "3"));
        datas0.add(new Node("4", "1", "最近一月", "4"));
        recyle_right0.setLayoutManager(new LinearLayoutManager(this));
        mAdapter0 = new SimpleTreeRecyclerAdapter(recyle_right0, this,
                datas0, 2, R.drawable.shipingziyuan_xiala_icon, R.drawable.xiugaixinxi_shouqi_icon);
        recyle_right0.setAdapter(mAdapter0);

        recyle_right.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SimpleTreeRecyclerAdapter(recyle_right, this,
                new ArrayList<>(), 1, R.drawable.shipingziyuan_xiala_icon, R.drawable.xiugaixinxi_shouqi_icon);
        recyle_right.setAdapter(mAdapter);
        mAdapter0.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
            @Override
            public void onClick(Node node, int position) {
                if (node == null) {
                    return;
                }
                if (!node.isLeaf()) {
                    return;
                }
                if ("2".equals(node.bean)) {
                    openRightLayout();
                    startTime = DateUtil.timeStampDay(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L);
                    endTime = DateUtil.timeStampDay(System.currentTimeMillis());
                    getData();
                } else if ("3".equals(node.bean)) {
                    openRightLayout();
                    startTime = DateUtil.timeStampDay(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);
                    endTime = DateUtil.timeStampDay(System.currentTimeMillis());
                    getData();
                } else if ("4".equals(node.bean)) {
                    openRightLayout();
                    startTime = DateUtil.timeStampDay(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L);
                    endTime = DateUtil.timeStampDay(System.currentTimeMillis());
                    getData();
                }
            }
        });
        mAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
            @Override
            public void onClick(Node node, int position) {
                if (node == null) {
                    return;
                }
                if (!node.isLeaf()) {
                    return;
                }
                if (node.bean instanceof DeviceBean) {
                    openRightLayout();
                    devId = ((DeviceBean) node.bean).deviceNo + "";
                    userId = null;
                    carId = null;
                    getData();
                } else if (node.bean instanceof UBean) {
                    openRightLayout();
                    userId = ((UBean) node.bean).userId + "";
                    devId = null;
                    carId = null;
                    getData();
                } else if (node.bean instanceof CarBean) {
                    openRightLayout();
                    carId = ((CarBean) node.bean).carId + "";
                    userId = null;
                    carId = null;
                    getData();
                }
            }
        });
        HttpUtil.getVideoList(new HttpUtil.Callback<VIdeoListBean>() {
            @Override
            public void onResponse(VIdeoListBean call) {
                vIdeoListBean = call;
                mAdapter.getAllNodes().clear();
                List<Node> mDatas = new ArrayList<>();
                if (call != null && call.data != null) {

                    for (int i = 0; i < call.data.size(); i++) {
                        VIdeoListBean.DataBean dataBean = call.data.get(i);
                        mDatas.add(new Node(dataBean.id, -1,
                                dataBean.name, dataBean));
                        if (dataBean.teamList != null) {
                            for (int j = 0; j < dataBean.teamList.size(); j++) {
                                VIdeoListBean.DataBean.TeamListBean teamListBean = dataBean.teamList.get(j);

                                mDatas.add(new Node(teamListBean.id, dataBean.id,
                                        teamListBean.name, teamListBean));

                                if (teamListBean.deviceList != null) {


                                    for (int k = 0; k < teamListBean.deviceList.size(); k++) {
                                        DeviceBean deviceBean = teamListBean.deviceList.get(k);
                                        mDatas.add(new Node(deviceBean.id, teamListBean.id,
                                                deviceBean.deviceName, deviceBean));

                                        if (k == 0) {
                                            devId = deviceBean.deviceNo + "";
                                            getData();
                                        }
                                    }


                                }
                                if (teamListBean.userList != null) {
                                    for (int k = 0; k < teamListBean.userList.size(); k++) {
                                        UBean deviceBean = teamListBean.userList.get(k);
                                        mDatas.add(new Node(deviceBean.id, teamListBean.id,
                                                deviceBean.realName, deviceBean));

//                                        if (k == 0 ){
//                                            devId = deviceBean.deviceNo+"";
//                                            getData();
//                                        }
                                    }
                                }

                                if (teamListBean.carList != null) {
                                    for (int k = 0; k < teamListBean.carList.size(); k++) {
                                        CarBean deviceBean = teamListBean.carList.get(k);
                                        mDatas.add(new Node(deviceBean.id, teamListBean.id,
                                                deviceBean.carId + "", deviceBean));

//                                        if (k == 0 ){
//                                            devId = deviceBean.deviceNo+"";
//                                            getData();
//                                        }
                                    }
                                }


                            }
                        }

                    }

                }

                mAdapter.addData(mDatas);
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

    private void getData() {
        showLoading(null);
        HttpUtil.getVideoHistoryListByDevIdAndTime(devId, userId, carId,
                startTime, endTime, null, null, new HttpUtil.Callback<VideoSourceBean>() {
                    @Override
                    public void onResponse(VideoSourceBean call) {
                        videoSourceBean = call;
                        if (call != null && call.data != null) {
                            pageNum = call.data.size() / 2;
                            if (pageNum > 0) {
                                cPageIndex = 0;
                            } else {
                                cPageIndex = -1;
                            }
                            initBottom();
                            setDataView(cPageIndex, call.data);
                        } else if (call != null) {
                            pageNum = 0;
                            cPageIndex = -1;
                            initBottom();
                            setDataView(cPageIndex, call.data);
                        }
                        dismissLoading();
                    }
                });

    }

    int pageNum = 0;
    int cPageIndex = 0;
    VideoSourceBean videoSourceBean;

    private void setDataView(int page, List<VideoSourceBean.DataBean> dataBeans) {
        int index = page * 2 - 2;
        if (dataBeans.size() - 1 >= index && page > 0) {
            VideoSourceBean.DataBean dataBean = dataBeans.get(index);
            ImageLoader.loadImage(this, video1_iv, dataBean.photoUrl, R.drawable.tu);
            MyUtil.setText(video1_tv, dataBean.name);
            video_1.setVisibility(View.VISIBLE);
            video_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RouterUtil.goVideoPlay(ZHShipinActivity.this, dataBean.name, dataBean.videoUrl, dataBean.photoUrl);
                }
            });
            if (dataBeans.size() - 1 < index + 1) {
                video_2.setVisibility(View.INVISIBLE);
                video_2.setOnClickListener(null);
            } else {
                video_2.setVisibility(View.VISIBLE);
                VideoSourceBean.DataBean dataBean2 = dataBeans.get(index + 1);
                ImageLoader.loadImage(this, video2_iv, dataBean.photoUrl, R.drawable.tu);
                MyUtil.setText(video2_tv, dataBean2.name);
                video_2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RouterUtil.goVideoPlay(ZHShipinActivity.this, dataBean2.name, dataBean.videoUrl, dataBean.photoUrl);
                    }
                });
            }
        } else if (dataBeans.size() > 0) {
            VideoSourceBean.DataBean dataBean = dataBeans.get(0);
            ImageLoader.loadImage(this, video1_iv, dataBean.photoUrl, R.drawable.tu);
            MyUtil.setText(video1_tv, dataBean.name);
            video_1.setVisibility(View.VISIBLE);
            video_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RouterUtil.goVideoPlay(ZHShipinActivity.this, dataBean.name, dataBean.videoUrl, dataBean.photoUrl);
                }
            });
            if (dataBeans.size() < 2) {
                video_2.setVisibility(View.INVISIBLE);
                video_2.setOnClickListener(null);
            } else {
                video_2.setVisibility(View.VISIBLE);
                VideoSourceBean.DataBean dataBean2 = dataBeans.get(1);
                ImageLoader.loadImage(this, video2_iv, dataBean.photoUrl, R.drawable.tu);
                MyUtil.setText(video2_tv, dataBean2.name);
                video_2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RouterUtil.goVideoPlay(ZHShipinActivity.this, dataBean2.name, dataBean2.videoUrl, dataBean.photoUrl);
                    }
                });
            }
        } else {
            video_1.setVisibility(View.INVISIBLE);
            video_2.setVisibility(View.INVISIBLE);
        }
    }

    private void initBottom() {
        group_bottom.removeAllViews();
        if (pageNum == 0) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        RadioButton r1 = (RadioButton) inflater.inflate(R.layout.zh_shipin_check_bottom, null);
        r1.setText("<");
        final int id1 = 1;
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 20;
        r1.setId(id1);
        group_bottom.addView(r1, layoutParams);
        for (int i = 0; i < pageNum; i++) {
            RadioButton r = (RadioButton) inflater.inflate(R.layout.zh_shipin_check_bottom, null);
            r.setText((i + 1) + "");
            group_bottom.addView(r, layoutParams);
            r.setId(i + 1);
        }
        RadioButton r2 = (RadioButton) inflater.inflate(R.layout.zh_shipin_check_bottom, null);
        r2.setText(">");
        final int id2 = pageNum + 2;
        r2.setId(id2);
        group_bottom.addView(r2, layoutParams);
        group_bottom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == id1) {
                    if (cPageIndex > 0) {
                        cPageIndex = cPageIndex - 1;
                    } else {
                        ToastUtils.showToast("没有更多数据了");
                    }
                } else if (checkedId == id2) {
                    if (cPageIndex < cPageIndex - 2) {
                        cPageIndex = cPageIndex + 1;
                    } else {
                        ToastUtils.showToast("没有更多数据了");
                    }
                } else {
                    cPageIndex = checkedId;
                    if (videoSourceBean != null) {
                        setDataView(cPageIndex, videoSourceBean.data);
                    }
                }
            }
        });
    }


}
