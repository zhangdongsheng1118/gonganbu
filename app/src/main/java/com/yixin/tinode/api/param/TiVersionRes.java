package com.yixin.tinode.api.param;

import java.util.List;

/**
 * Created by Administrator on 2018/7/31 0031.
 * 通用数据,返回数据
 */

public class TiVersionRes {
    private List<TiAppModule> apps;
    private TiSetting setting;

    public List<TiAppModule> getApps() {
        return apps;
    }

    public void setApps(List<TiAppModule> apps) {
        this.apps = apps;
    }

    public TiSetting getSetting() {
        return setting;
    }

    public void setSetting(TiSetting setting) {
        this.setting = setting;
    }
}
