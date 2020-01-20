package com.zuozhan.app.httpUtils;

import com.zuozhan.app.bean.ArticleBean;
import com.zuozhan.app.bean.BaseBean;
import com.zuozhan.app.bean.DuijiangBean;
import com.zuozhan.app.bean.EeceptionBean;
import com.zuozhan.app.bean.FileBean;
import com.zuozhan.app.bean.JiaoLiuBean;
import com.zuozhan.app.bean.RenWuLeiBean;
import com.zuozhan.app.bean.UserBean;
import com.zuozhan.app.bean.UserTreeBean;
import com.zuozhan.app.bean.VIdeoListBean;
import com.zuozhan.app.bean.VersionBean;
import com.zuozhan.app.bean.VideoSourceBean;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface MyService {
    //登录接口
    @POST("user/login")
    Call<UserBean> getLogin(@Query("username") String username,
                            @Query("password") String password);

    @POST("command/combat/selectVersion")
    Call<VersionBean> selectVersion();

    //登陆成功人员的信息
    @GET("command/user/getUserInfoByToken")
    Call<UserBean> getUserInfoByToken(@Query("Authorization") String Authorization);

    @GET("command/user/getHeadPicById")
    Call<UserBean> getHeadPicById(@Query("id") String id);

    //任务列表
    @GET("gonganbu/admin/mission/getMyMissionByTokenAndInfo")
    Call<RenWuLeiBean> getRenWu(@Query("Authorization") String Authorization,
                                @Query("status") String status);

    //任务列表
    @GET("gonganbu/admin/mission/getMyMissionByTokenAndInfo")
    Call<RenWuLeiBean> getRenWu(@Query("Authorization") String Authorization,
                                @Query("status") String status, @Query("responsibleUserName") String responsibleUserName
            , @Query("name") String name);

    //任务列表
    @GET("gonganbu/admin/mission/getMyMissionByTokenAndInfo")
    Call<RenWuLeiBean> getMyMissionByTokenAndCondition();

    //任务列表
    @GET("command/combat/getAllCaseList")
    Call<RenWuLeiBean> getRenWuAll(@Query("pageSize") String pageSize,
                                   @Query("pageNumber") String pageNumber);


    @GET("user/updatePassword")
    Call<BaseBean> updatePassword(@Query("password") String password,
                                  @Query("newPassword") String newPassword);

    //类别
    @GET("command/article/getType?type=1")
    Call<ArticleBean> getType1();

    //工作交流
    @GET("command/article/getArticle")
    Call<ArticleBean> getArticle(@Query("type") String type);

    @GET("command/article/getArticle")
    Call<ArticleBean> getArticle(@Query("type") String type, @Query("title") String title);

    @GET("command/article/getArticleByTypeByType?type=1")
    Call<ArticleBean> getArticleByTypeByType();

    @GET("command/user/updateUserInfo")
    Call<UserBean> updateUserInfo(@Query("id") String id,
                                  @Query("headPic") String headPic,
                                  @Query("realName") String realName,
                                  @Query("phone") String phone,
                                  @Query("departmentName") String departmentName,
                                  @Query("departmentId") String departmentId,
                                  @Query("duty") String duty);


    @Multipart
    @POST("command/uploadFile")
    Call<FileBean> uploadFile(@Part() MultipartBody.Part part);

    @GET("command/user/getUserTree")
    Call<UserTreeBean> getUserTree();


    @GET("/command/combat/getVideoHistoryListByDevIdAndTime")
    Call<VideoSourceBean> getVideoHistoryListByDevIdAndTime(@Query("devId") String devId,
                                                            @Query("userId") String userId,
                                                            @Query("carId") String carId,
                                                            @Query("startTime") String startTime,
                                                            @Query("endTime") String endTime,
                                                            @Query("pageSize") String pageSize,
                                                            @Query("pageNumber") String pageNumber);

    @GET("gonganbu/emegencyNotice/addEmegencyNoticeByToken?type=2")
    Call<BaseBean> addEmegencyNotice(@Query("name") String name,
                                     @Query("content") String content,
                                     @Query("status") String status,
                                     @Query("picUrl") String picUrl);

    @GET("gonganbu/admin/callPolice/updateCallPolice?status=2")
    Call<BaseBean> removeEmegencyNotice(@Query("id") String id);

    @GET("/gonganbu/emegencyNotice/getEmegencyNoticeList")
    Call<EeceptionBean> getCallPoliceList(@Query("caseId") String caseId);

    @GET("/command/user/getUserBySUid")
    Call<UserBean> getUserBySUid(@Query("suid") String suid);

    @GET("command/combat/getMyMissionAndDeviceAndUserByTokenForAppVideo")
    Call<VIdeoListBean> getMyMissionAndDeviceAndUserByTokenForAppVideo();

    @GET("command/combat/addIntercomInfoByToken")
    Call<BaseBean> addIntercomInfoByToken(@Query("missionId") String missionId,
                                          @Query("intercomId") String intercomId,
                                          @Query("name") String name);

    @GET("command/combat/selectIntercomInfoByTokenAndCaseId")
    Call<DuijiangBean> selectIntercomInfoByTokenAndCaseId(@Query("type") String type,@Query("missionId") String missionId);

    @GET("command/combat/addKeyNodesByToken")
    Call<BaseBean> addKeyNodesByToken(@Query("caseId") String caseId,
                                      @Query("missionId") String missionId,
                                      @Query("longitude") String longitude,
                                      @Query("latitude") String latitude,
                                      @Query("description") String description);
}
