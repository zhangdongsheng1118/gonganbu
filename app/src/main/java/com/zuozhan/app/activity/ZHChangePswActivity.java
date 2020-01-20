package com.zuozhan.app.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.yixin.tinode.R;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.adapter.TongZhiAdapter;
import com.zuozhan.app.bean.ArticleBean;
import com.zuozhan.app.bean.BaseBean;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.httpUtils.MyService;
import com.zuozhan.app.net.HttpService;
import com.zuozhan.app.util.ToastUtils;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZHChangePswActivity extends AllBaseActivity {

    @BindView(R.id.back_img)
    ImageView imageView;
    @BindView(R.id.change_psw1)
    EditText change_psw1;
    @BindView(R.id.change_psw2)
    EditText change_psw2;
    @BindView(R.id.change_psw3)
    EditText change_psw3;
    @BindView(R.id.login_Btn)
    View login_Btn;

    private SharedPreferences sp;
    private String numbers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_change_psw);

        SharedPreferences  sp = getSharedPreferences("login_passs", Context.MODE_PRIVATE);
        numbers = sp.getString("user_mobile", "");
       /* Toast.makeText(ZHChangePswActivity.this, ""+numbers, Toast.LENGTH_SHORT).show();
        Log.i("", "dddddddd"+numbers);
*/
        //点击返回箭头关闭此页面
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        login_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str1 = change_psw1.getText().toString();
                String str2 = change_psw2.getText().toString();
                String str3 = change_psw3.getText().toString();

                if (TextUtils.isEmpty(str1)) {
                    ToastUtils.showToast("旧密码不能为空");
                    return;
                }
                if (!str1.equals(numbers)) {
                    ToastUtils.showToast("旧密码错误");
                    return;
                }

                if (TextUtils.isEmpty(str2)) {
                    ToastUtils.showToast("新密码不能为空");
                    return;
                }
                if (TextUtils.isEmpty(str3)) {
                    ToastUtils.showToast("再次输入密码不能为空");
                    return;
                }
                if (!str2.equals(str3)) {
                    ToastUtils.showToast("两次输入的密码不一致");
                    return;
                }

                HttpUtil.updatePassword(str1, str2, new HttpUtil.Callback<BaseBean>() {
                    @Override
                    public void onResponse(BaseBean call) {
                        if (call != null && call.code == 1) {
                            ToastUtils.showToast("修改完成，请重新登录");
                            RouterUtil.goLoginToClear(ZHChangePswActivity.this);
                        } else {
                            ToastUtils.showToast("修改失败，请输入正确的密码");
                        }
                    }
                });
            }
        });
    }
}
