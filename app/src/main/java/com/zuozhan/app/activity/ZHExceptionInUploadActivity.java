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
import com.yixin.tinode.util.PopupWindowUtils;
import com.zuozhan.app.AppEnvirment;
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

public class ZHExceptionInUploadActivity extends AllBaseActivity {

    @BindView(R.id.title_left)
    ImageView imageView;
    @BindView(R.id.iv_showimg0)
    ImageView iv_showimg0;
    @BindView(R.id.iv_showimg)
    ImageView iv_showimg;
    @BindView(R.id.c_name)
    EditText c_name;
    @BindView(R.id.c_uploadname)
    EditText c_uploadname;
    @BindView(R.id.c_info)
    EditText c_info;

    @BindView(R.id.c_submit)
    View c_submit;
    String headPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_exception_upload);

        //点击返回箭头关闭此页面
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iv_showimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ZHExceptionInUploadActivity.this, ImageGridActivity.class);
                startActivityForResult(intent, 1000);
            }
        });
        iv_showimg0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ZHExceptionInUploadActivity.this, ImageGridActivity.class);
                startActivityForResult(intent, 1000);
            }
        });
//        ImageLoader.loadImage(this, toux, userBean.data.headPic);
        c_uploadname.setEnabled(false);
        MyUtil.setText(c_uploadname, AppEnvirment.getUserBean().data.username);
//        MyUtil.setText(c_name, userBean.data.realName);
//        MyUtil.setText(c_phone, userBean.data.phone);
//        MyUtil.setText(c_zuzhi, userBean.data.departmentName);
//        MyUtil.setText(c_zhiwei, userBean.data.duty);

        c_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str1 = c_name.getText().toString();
                String str2 = c_uploadname.getText().toString();
                String str3 = c_info.getText().toString();
                if (TextUtils.isEmpty(str1)) {
                    ToastUtils.showToast("异常名称不能为空");
                    return;
                }
                if (TextUtils.isEmpty(str2)) {
                    ToastUtils.showToast("提交人不能为空");
                    return;
                }
                if (TextUtils.isEmpty(str3)) {
                    ToastUtils.showToast("描述不能为空");
                    return;
                }

                HttpUtil.addEmegencyNotice(str1, str3, "1", headPic
                        , new HttpUtil.Callback<BaseBean>() {
                            @Override
                            public void onResponse(BaseBean call) {
                                try {
                                    if (call != null && call.code == 1) {
                                        ToastUtils.showToast("上报成功");
                                        finish();
                                    } else {
                                        ToastUtils.showToast("上报失败");
                                        finish();
                                    }
                                } catch (Exception e) {
                                    ToastUtils.showToast("上报失败");
                                    finish();
                                }
                            }
                        });
            }
        });

    }


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
                            iv_showimg0.setVisibility(View.GONE);
                            iv_showimg.setVisibility(View.VISIBLE);
                            ImageLoader.loadImage(this, iv_showimg, imageItem.path);
                            showLoading(null);
                            HttpUtil.uploadFile(imageItem.path, new HttpUtil.Callback<FileBean>() {
                                @Override
                                public void onResponse(FileBean call) {
                                    dismissLoading();
                                    if (call != null && call.data != null) {
                                        ToastUtils.showToast("上传成功");
                                        headPic = call.data;
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

}
