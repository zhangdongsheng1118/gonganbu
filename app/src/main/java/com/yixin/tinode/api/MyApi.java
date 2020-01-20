package com.yixin.tinode.api;


import com.yixin.tinode.api.param.ReturnResult;
import com.yixin.tinode.api.param.TiCollection;
import com.yixin.tinode.api.param.TiSetting;
import com.yixin.tinode.api.param.TiVersion;
import com.yixin.tinode.api.param.TiVersionRes;
import com.zuozhan.app.BaseIP;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * @创建者 CSDN_LQR
 * @描述 server端api
 */

public interface MyApi {

    public static final String BASE_URL = BaseIP.IM_IP_2;
//    public static final String BASE_URL = "http://192.168.43.16:8090";

//    public static final String BASE_URL = UpdateManager.BaseUrl;

    //////////////////////////使用中//////////////////////////////////////////////////////////////////
    //下载图片
    @Streaming
    @GET
    Observable<ResponseBody> downloadPic(@Url String url);

    @POST("appset/addSetting")
    Observable<ReturnResult> addSetting(@Body TiSetting body);

    @POST("appset/checkVersion")
    Observable<ReturnResult<TiVersionRes>> checkVersion(@Body TiVersion body);

    @POST("api/v1/collections/add")
    Observable<ReturnResult> addCollect(@Body TiCollection body);

    @POST("api/v1/collections/del")
    Observable<ReturnResult> delCollect(@Body TiCollection body);

    @POST("api/v1/collections/list")
    Observable<ReturnResult<List<TiCollection>>> listCollect(@Body TiCollection body);
}
