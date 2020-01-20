package com.yixin.tinode.ui.activity;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.kyleduo.switchbutton.SwitchButton;
import com.yixin.tinode.R;
import com.yixin.tinode.api.param.TiSetting;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.manager.JsonMananger;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.SPUtils;
import com.zhy.autolayout.AutoLinearLayout;

import butterknife.BindView;
import retrofit2.adapter.rxjava.HttpException;

/**
 * @创建者 CSDN_LQR
 * @描述 新消息提醒设置
 */
public class SettingNewMsgNotifyActivity extends BaseActivity {

    @BindView(R.id.sb_show_notify)
    SwitchButton mSbShowNotify;
    @BindView(R.id.sb_sound)
    SwitchButton mSbSound;
    @BindView(R.id.sb_vibrate)
    SwitchButton mSbVibrate;
    @BindView(R.id.al_sound)
    AutoLinearLayout mAlSound;
    @BindView(R.id.al_vibrate)
    AutoLinearLayout mAlVibrate;
    @BindView(R.id.spFile)
    Spinner spFile;

    private TiSetting settingNewMsgNotify;

    public static TiSetting getSettingNotify(Context context) {
        TiSetting settingNewMsgNotify = null;
        String topic = Cache.getTinode().getMyId();
        String key = AppConst.SP_SETTING_NOTIFY_KEY_PRE + topic;
        String saved = SPUtils.getInstance(context).getString(key, null);
        if (saved == null) {
            settingNewMsgNotify = new TiSetting();
            settingNewMsgNotify.setNotification(AppConst.TRUE);
            settingNewMsgNotify.setSound(AppConst.TRUE);
            settingNewMsgNotify.setVibrate(AppConst.TRUE);
            settingNewMsgNotify.setTopic(topic);
            save(context, settingNewMsgNotify);
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
            SPUtils.getInstance(context).putString(AppConst.SP_SETTING_NOTIFY_KEY_PRE + Cache.getTinode().getMyId(), JsonMananger.beanToJson(settingNewMsgNotify));
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initListener() {
        String topic = Cache.getTinode().getMyId();
        String key = AppConst.SP_SETTING_NOTIFY_KEY_PRE + topic;
        String saved = SPUtils.getInstance(context).getString(key, null);
        if (saved == null) {
            settingNewMsgNotify = new TiSetting();
            settingNewMsgNotify.setNotification(AppConst.TRUE);
            settingNewMsgNotify.setSound(AppConst.TRUE);
            settingNewMsgNotify.setVibrate(AppConst.TRUE);
            settingNewMsgNotify.setTopic(topic);
            save(context, settingNewMsgNotify);
        } else {
            try {
                settingNewMsgNotify = JsonMananger.jsonToBean(saved, TiSetting.class);
            } catch (HttpException e) {
                e.printStackTrace();
                settingNewMsgNotify = new TiSetting();
                settingNewMsgNotify.setNotification(AppConst.TRUE);
                settingNewMsgNotify.setSound(AppConst.TRUE);
                settingNewMsgNotify.setVibrate(AppConst.TRUE);
                settingNewMsgNotify.setTopic(topic);
                save(context, settingNewMsgNotify);
            }
        }

        if (settingNewMsgNotify.getNotification() == AppConst.TRUE) {
            mSbShowNotify.setChecked(true);
            mSbVibrate.setChecked(settingNewMsgNotify.getVibrate() == AppConst.TRUE);
            mSbSound.setChecked(settingNewMsgNotify.getSound() == AppConst.TRUE);
        } else {
            mSbShowNotify.setChecked(false);
            mSbVibrate.setChecked(settingNewMsgNotify.getVibrate() == AppConst.TRUE);
            mSbSound.setChecked(settingNewMsgNotify.getSound() == AppConst.TRUE);
            mAlSound.setVisibility(View.GONE);
            mAlVibrate.setVisibility(View.GONE);
        }

        mSbShowNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mAlVibrate.setVisibility(View.VISIBLE);
                    mAlSound.setVisibility(View.VISIBLE);
                } else {
                    mAlVibrate.setVisibility(View.GONE);
                    mAlSound.setVisibility(View.GONE);
                }

                settingNewMsgNotify.setNotification(b ? 1 : 0);
                save(context, settingNewMsgNotify);
            }
        });
        mSbSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settingNewMsgNotify.setSound(b ? 1 : 0);
                save(context, settingNewMsgNotify);
            }
        });
        mSbVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settingNewMsgNotify.setVibrate(b ? 1 : 0);
                save(context, settingNewMsgNotify);
            }
        });

        spFile.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, new String[]{AppConst.SEND_FILE_TYPE_TINODE, AppConst.SEND_FILE_TYPE_SEAWEED,}));
        spFile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settingNewMsgNotify.setSendFileType((String) parent.getAdapter().getItem(position));
                save(context, settingNewMsgNotify);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (settingNewMsgNotify.getSendFileType() != null) {
            spFile.setSelection(AppConst.SEND_FILE_TYPE_SEAWEED.equals(settingNewMsgNotify.getSendFileType()) ? 1 : 0);
        }
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_setting_new_msg_notify;
    }
}
