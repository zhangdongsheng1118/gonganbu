package com.yixin.tinode.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yixin.tinode.R;
import com.yixin.tinode.api.ApiRetrofit;
import com.yixin.tinode.api.param.TiSetting;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.manager.JsonMananger;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.SPUtils;
import com.zhy.autolayout.AutoLinearLayout;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @创建者 CSDN_LQR
 * @描述 新消息提醒设置
 */
public class SettingAVActivity extends BaseActivity {

    @BindView(R.id.al_avserver_address)
    AutoLinearLayout mAVServerAddr;
    @BindView(R.id.al_avserver_port)
    AutoLinearLayout mAVServerPort;
    @BindView(R.id.al_display)
    AutoLinearLayout mAVDisplay;
    @BindView(R.id.al_textview_address)
    TextView mTextViewAddr;
    @BindView(R.id.al_textview_avport)
    TextView mTextViewAVPort;
    @BindView(R.id.al_textview_display)
    TextView mTextViewDisplay;

    private TiSetting settingNewMsgNotify;

    public static TiSetting getSettingNotify(Context context) {
        TiSetting settingNewMsgNotify = null;
        String topic = Cache.getTinode().getMyId();
        String key = AppConst.SP_SETTING_AV_KEY_PRE + topic;
        String saved = SPUtils.getInstance(context).getString(key, null);
        Log.i("SettingAVActivity","71");
        //Log.i("SettingAVActivity gsn",saved);
        if (saved == null) {
            settingNewMsgNotify = new TiSetting();
        } else {
            try {
                settingNewMsgNotify = JsonMananger.jsonToBean(saved, TiSetting.class);
            } catch (HttpException e) {
                e.printStackTrace();
            }
        }
        return settingNewMsgNotify;
    }

    public static void save(Context context, TiSetting settingNewMsgNotify) {
        try {
            String str=AppConst.SP_SETTING_AV_KEY_PRE + Cache.getTinode().getMyId();
            Log.i("SettingAVActivity","88");
            SPUtils.getInstance(context).putString(AppConst.SP_SETTING_AV_KEY_PRE + Cache.getTinode().getMyId(), JsonMananger.beanToJson(settingNewMsgNotify));
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initListener() {
        String topic = Cache.getTinode().getMyId();
        String key = AppConst.SP_SETTING_AV_KEY_PRE + topic;
        String saved = SPUtils.getInstance(context).getString(key, null);
        if (saved == null) {
            Log.i("SettingAVActivity","102");
            settingNewMsgNotify = new TiSetting();
            settingNewMsgNotify.setAvServerAddr(AppConst.AV_ADDRESS);
            settingNewMsgNotify.setAvServerPort(AppConst.AV_PORT);
            settingNewMsgNotify.setAvQuality(AppConst.AV_QUALITY);
            save(context, settingNewMsgNotify);
        } else {
            try {
                Log.i("SettingAVActivity","110");
                settingNewMsgNotify = JsonMananger.jsonToBean(saved, TiSetting.class);
            } catch (HttpException e) {
                Log.i("SettingAVActivity","113");
                e.printStackTrace();
                settingNewMsgNotify = new TiSetting();
                settingNewMsgNotify.setAvServerAddr(AppConst.AV_ADDRESS);
                settingNewMsgNotify.setAvServerPort(AppConst.AV_PORT);
                settingNewMsgNotify.setAvQuality(AppConst.AV_QUALITY);
                save(context, settingNewMsgNotify);
            }
        }

        mTextViewAddr.setText(settingNewMsgNotify.getAvServerAddr());
        mTextViewAVPort.setText(settingNewMsgNotify.getAvServerPort());
        mTextViewDisplay.setText(settingNewMsgNotify.getAvQuality());
    }

    private void updateHttp() {
        ApiRetrofit.getInstance().mApi.addSetting(settingNewMsgNotify)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sendCodeResponse -> {
                    if (sendCodeResponse.success()) {

                    } else {
                        LogUtils.e(new Gson().toJson(sendCodeResponse));
                    }
                }, error -> {
                    LogUtils.e(error.getLocalizedMessage());
                });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_setting_av;
    }

    @OnClick({R.id.al_avserver_address,R.id.al_avserver_port,R.id.al_display})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.al_avserver_address:
                //dialog with TextEdit for address
                showInputDialog(mTextViewAddr.getText().toString());
                break;
            case R.id.al_avserver_port:
                //dialog with TextEdit for port
                showAVPortInputDialog(mTextViewAVPort.getText().toString());
                break;
            case R.id.al_display:
                //dialog with TextEdit for address
                showSingleChoiceDialog();
                break;
        }
    }

    private void showInputDialog(String addr) {
        final EditText editText = new EditText(context);
        editText.setText(addr);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(context);
        inputDialog.setTitle("音视频服务器地址").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context,
                                editText.getText().toString(),
                                Toast.LENGTH_SHORT).show();
                        mTextViewAddr.setText(editText.getText().toString());
                        settingNewMsgNotify.setAvServerAddr(mTextViewAddr.getText().toString());
                        save(context, settingNewMsgNotify);
                    }
                }).show();
    }

    private void showAVPortInputDialog(String port) {
        final EditText editText = new EditText(context);
        editText.setText(port);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(context);
        inputDialog.setTitle("音视频服务器端口").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context,
                                editText.getText().toString(),
                                Toast.LENGTH_SHORT).show();
                        mTextViewAVPort.setText(editText.getText().toString());
                        settingNewMsgNotify.setAvServerPort(mTextViewAVPort.getText().toString());
                        save(context, settingNewMsgNotify);
                    }
                }).show();
    }

    private int yourChoice;
    private void showSingleChoiceDialog(){
        final String[] items = { "低","中","高" };
        yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(context);
        singleChoiceDialog.setTitle("视频质量选择:");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (yourChoice != -1) {
                            Toast.makeText(context,
                                    "你选择了" + items[yourChoice],
                                    Toast.LENGTH_SHORT).show();
                            mTextViewDisplay.setText(items[yourChoice]);
                            settingNewMsgNotify.setAvQuality(mTextViewDisplay.getText().toString());
                            save(context, settingNewMsgNotify);
                        }
                    }
                });
        singleChoiceDialog.show();
    }


}
