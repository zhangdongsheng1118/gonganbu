package com.zuozhan.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.ui.activity.CreateGroupActivity;
import com.yixin.tinode.ui.activity.SearchUserActivity;
import com.yixin.tinode.ui.fragment.RecentMessageFragment;
import com.yixin.tinode.util.PopupWindowUtils;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.adapter.ArticleRecyclerAdapter;
import com.zuozhan.app.bean.BaseBean;
import com.zuozhan.app.bean.FileBean;
import com.zuozhan.app.bean.MyUtil;
import com.zuozhan.app.bean.UserBean;
import com.zuozhan.app.bean.UserTreeBean;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.imageloader.ImageLoader;
import com.zuozhan.app.photo.imagepicker.ImagePicker;
import com.zuozhan.app.photo.imagepicker.bean.ImageItem;
import com.zuozhan.app.photo.imagepicker.ui.ImageGridActivity;
import com.zuozhan.app.treelist.Node;
import com.zuozhan.app.treelist.OnTreeNodeClickListener;
import com.zuozhan.app.treelist.SimpleTreeRecyclerAdapter;
import com.zuozhan.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import co.tinode.tinodesdk.MeTopic;

public class ZHChangeUserActivity extends AllBaseActivity {

    @BindView(R.id.title_left)
    ImageView imageView;
    @BindView(R.id.toux)
    ImageView toux;
    @BindView(R.id.changge)
    View changge;
    @BindView(R.id.c_name)
    EditText c_name;
    @BindView(R.id.c_phone)
    EditText c_phone;
    @BindView(R.id.c_zuzhi)
    TextView c_zuzhi;
    @BindView(R.id.c_zhiwei)
    EditText c_zhiwei;
    @BindView(R.id.c_bumen)
    TextView c_bumen;
    @BindView(R.id.c_submit)
    View c_submit;
    String realName;
    String phone;
    String departmentName;
    String departmentId;
    String duty;
    String headPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_xiu_gai);

        //点击返回箭头关闭此页面
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        UserBean userBean = AppEnvirment.getUserBean();
        if (userBean == null || userBean.data == null) {
            return;
        }
        c_zuzhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopu(v, mDatas);
            }
        });
        toux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ZHChangeUserActivity.this, ImageGridActivity.class);
                startActivityForResult(intent, 1000);
            }
        });
        changge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ZHChangeUserActivity.this, ImageGridActivity.class);
                startActivityForResult(intent, 1000);
            }
        });
        if (BaseIP.isDebug) {
            ToastUtils.showToast("(测试模式) 原头像地址:" + userBean.data.headPic);
        }
        ImageLoader.loadImage(this, toux, userBean.data.headPic);
        MyUtil.setText(c_bumen, userBean.data.username);
        MyUtil.setText(c_name, userBean.data.realName);
        MyUtil.setText(c_phone, userBean.data.phone);
        MyUtil.setText(c_zuzhi, userBean.data.departmentName);
        MyUtil.setText(c_zhiwei, userBean.data.duty);

        headPic = userBean.data.headPic;
        realName = userBean.data.realName;
        phone = userBean.data.phone;
        departmentName = userBean.data.departmentName;
        departmentId = userBean.data.departmentId;
        duty = userBean.data.duty;

        c_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str1 = c_name.getText().toString();
                String str2 = c_phone.getText().toString();
                String str3 = c_zuzhi.getText().toString();
                String str4 = c_zhiwei.getText().toString();
                if (TextUtils.isEmpty(str1)) {
                    ToastUtils.showToast("姓名不能为空");
                    return;
                }

                boolean isPhoneNum = isMobileNO(str2);
                if (TextUtils.isEmpty(str2)){
                    ToastUtils.showToast("联系电话不能为空");
                    return;
                }else if (!isPhoneNum){
                    c_phone.setText("");
                    ToastUtils.showToast("请输入有效的手机号码！");
                    return;
                }

                if (TextUtils.isEmpty(str3)) {
                    ToastUtils.showToast("组织不能为空");
                    return;
                }
                if (TextUtils.isEmpty(str4)) {
                    ToastUtils.showToast("职位不能为空");
                    return;
                }
                realName = str1;
                phone = str2;
                duty = str4;
                if (BaseIP.isDebug) {
                    ToastUtils.showToast("(测试模式) 上传新头像地址:" + headPic);
                }
                HttpUtil.updateUserInfo(userBean.data.id + "",
                        headPic, realName, phone, departmentName, departmentId, duty, new HttpUtil.Callback<BaseBean>() {
                            @Override
                            public void onResponse(BaseBean call) {
                                if (call != null && call.code == 1) {
                                    HttpUtil.getUserInfo(new HttpUtil.Callback<UserBean>() {
                                        @Override
                                        public void onResponse(UserBean call) {
                                            ToastUtils.showToast("修改完成");
                                            finish();
                                        }
                                    });
                                } else {
                                    ToastUtils.showToast("修改失败");
                                    finish();
                                }
                            }
                        });
            }
        });

        HttpUtil.getUserTree(new HttpUtil.Callback<UserTreeBean>() {
            @Override
            public void onResponse(UserTreeBean call) {
                mDatas.clear();
                if (call == null || call.data == null) {
                    return;
                }
                List<UserTreeBean.ChildNode> list = new ArrayList<>();
                for (int i = 0; i < call.data.size(); i++) {
                    getList(list, call.data.get(i));
                }
                for (int i = 0; i < list.size(); i++) {
                    mDatas.add(new Node(list.get(i).id, list.get(i).parentId, list.get(i).name,
                            list.get(i).id + "", null));
                }

            }

            private void getList(List<UserTreeBean.ChildNode> list, UserTreeBean.ChildNode node) {
                if (node != null && node.childNode != null) {
                    list.add(node);
                    for (int i = 0; i < node.childNode.size(); i++) {
                        getList(list, node.childNode.get(i));
                    }
                }


            }
        });
    }

    List<Node> mDatas = new ArrayList<>();


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1000:
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                    if (data != null) {
                        final MeTopic me = Cache.getTinode().getMeTopic();
                        ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                        if (images != null && images.size() > 0) {
                            ImageItem imageItem = images.get(0);
                            ImageLoader.loadImage(this, toux, imageItem.path);
                            showLoading(null);
                            HttpUtil.uploadFile(imageItem.path, new HttpUtil.Callback<FileBean>() {
                                @Override
                                public void onResponse(FileBean call) {
                                    dismissLoading();
                                    if (call != null && call.data != null) {
                                        headPic = call.data;
                                        ToastUtils.showToast("上传成功");
                                    } else {
                                        ToastUtils.showToast("上传失败");
                                    }
                                }
                            });
                        }
                    }
                }
        }
    }

    private void showPopu(View view, List<Node> nodes) {
        View menuView = View.inflate(this, R.layout.list_popuwindow, null);
        RecyclerView recycle = menuView.findViewById(R.id.recycle);

        PopupWindow popupWindow = PopupWindowUtils.getPopupWindowAsDropDown(menuView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, view,
                0, 0);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recycle.setLayoutManager(manager);
        SimpleTreeRecyclerAdapter adapter = new SimpleTreeRecyclerAdapter(recycle, this,
                new ArrayList<>(), 1, R.drawable.shipingziyuan_xiala_icon, R.drawable.xiugaixinxi_shouqi_icon);
        recycle.setAdapter(adapter);

        adapter.getAllNodes().clear();
        adapter.addData(nodes);
        adapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
            @Override
            public void onClick(Node node, int position) {
                if (node.isLeaf()) {
                    departmentName = node.getName();
                    departmentId = node.getId() + "";
                    c_zuzhi.setText(departmentName);
                    popupWindow.dismiss();
                }
            }
        });
    }


    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */
        String telRegex = "[1][3456789]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles))
            return false;
        else
            return mobiles.matches(telRegex);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
