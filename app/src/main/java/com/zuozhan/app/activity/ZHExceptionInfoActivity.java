package com.zuozhan.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.bean.BaseBean;
import com.zuozhan.app.bean.EeceptionBean;
import com.zuozhan.app.bean.MyUtil;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.imageloader.ImageLoader;
import com.zuozhan.app.util.ToastUtils;

import butterknife.BindView;

public class ZHExceptionInfoActivity extends AllBaseActivity {

    @BindView(R.id.title_left)
    ImageView imageView;
    @BindView(R.id.iv_showimg)
    ImageView iv_showimg;
    @BindView(R.id.c_name)
    TextView c_name;
    @BindView(R.id.c_uploadname)
    TextView c_uploadname;
    @BindView(R.id.c_info)
    TextView c_info;

    @BindView(R.id.c_submit)
    View c_submit;

    EeceptionBean.DataBean dataBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_exception_info);
        dataBean = (EeceptionBean.DataBean) getIntent().getSerializableExtra("info");
        //点击返回箭头关闭此页面
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageLoader.loadImage(this, iv_showimg, dataBean.picUrl);
        MyUtil.setText(c_name, dataBean.name);
        MyUtil.setText(c_uploadname, dataBean.createUserName);
        MyUtil.setText(c_info, dataBean.content);

        c_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.removeEmegencyNotice(dataBean.id + "", new HttpUtil.Callback<BaseBean>() {
                    @Override
                    public void onResponse(BaseBean call) {
                        if (call != null && call.code == 1) {
                            ToastUtils.showToast("解除成功");
                            finish();
                        } else {
                            ToastUtils.showToast("解除失败");
                            finish();
                        }
                    }
                });
            }
        });

        if (dataBean.status == 2) {
            c_submit.setVisibility(View.GONE);
        } else {
            c_submit.setVisibility(View.VISIBLE);
        }
    }

}
