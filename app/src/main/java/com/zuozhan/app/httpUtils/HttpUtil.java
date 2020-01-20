package com.zuozhan.app.httpUtils;

import com.google.gson.Gson;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.bean.ArticleBean;
import com.zuozhan.app.bean.BaseBean;
import com.zuozhan.app.bean.DuijiangBean;
import com.zuozhan.app.bean.FileBean;
import com.zuozhan.app.bean.RenWuLeiBean;
import com.zuozhan.app.bean.UserBean;
import com.zuozhan.app.bean.UserTreeBean;
import com.zuozhan.app.bean.VIdeoListBean;
import com.zuozhan.app.bean.VideoSourceBean;
import com.zuozhan.app.net.HttpService;
import com.zuozhan.app.util.AppManager;
import com.zuozhan.app.util.ShareprefrensUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Query;

public class HttpUtil {

    public static void getUserInfo(Callback<UserBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getUserInfoByToken(AppEnvirment.getToken())
                .enqueue(new retrofit2.Callback<UserBean>() {
                    @Override
                    public void onResponse(Call<UserBean> call, Response<UserBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }

                        AppEnvirment.userBean = response.body();
                        ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.USERINFO, new Gson().toJson(AppEnvirment.userBean));
                        callback.onResponse(AppEnvirment.userBean);
                    }

                    @Override
                    public void onFailure(Call<UserBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void getUserInfo(String token,Callback<UserBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getUserInfoByToken(token)
                .enqueue(new retrofit2.Callback<UserBean>() {
                    @Override
                    public void onResponse(Call<UserBean> call, Response<UserBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            callback.onResponse(response.body());
                            return;
                        }
                        if (response.body()!=null){
                            AppEnvirment.userBean = response.body();
                            ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.USERINFO, new Gson().toJson(AppEnvirment.userBean));
                        }
                        callback.onResponse(AppEnvirment.userBean);
                    }

                    @Override
                    public void onFailure(Call<UserBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void updatePassword(String pass, String newpass, Callback<BaseBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().updatePassword(pass, newpass)
                .enqueue(new retrofit2.Callback<BaseBean>() {
                    @Override
                    public void onResponse(Call<BaseBean> call, Response<BaseBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        BaseBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<BaseBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void getUserTree(Callback<UserTreeBean> callback) {
        HttpService.getApi(MyService.class)
                .getServiceApi().getUserTree()
                .enqueue(new retrofit2.Callback<UserTreeBean>() {
                    @Override
                    public void onResponse(Call<UserTreeBean> call, Response<UserTreeBean> response) {
                        UserTreeBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<UserTreeBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void getHeadPicById(String id,Callback<UserBean> callback) {
        HttpService.getApi(MyService.class)
                .getServiceApi().getHeadPicById(id)
                .enqueue(new retrofit2.Callback<UserBean>() {
                    @Override
                    public void onResponse(Call<UserBean> call, Response<UserBean> response) {
                        UserBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<UserBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void updateUserInfo(String id,
                                      String headPic,
                                      String realName,
                                      String phone,
                                      String departmentName,
                                      String departmentId,
                                      String duty, Callback<BaseBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().updateUserInfo(id, headPic, realName, phone, departmentName, departmentId, duty)
                .enqueue(new retrofit2.Callback<UserBean>() {
                    @Override
                    public void onResponse(Call<UserBean> call, Response<UserBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        UserBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<UserBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void uploadFile(String path, Callback<FileBean> callback) {
        final File file = new File(path);
        final RequestBody requestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);
        final MultipartBody.Part part =
                MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().uploadFile(part)
                .enqueue(new retrofit2.Callback<FileBean>() {
                    @Override
                    public void onResponse(Call<FileBean> call, Response<FileBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        FileBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<FileBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }


    public static void getType1(Callback<ArticleBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getType1()
                .enqueue(new retrofit2.Callback<ArticleBean>() {
                    @Override
                    public void onResponse(Call<ArticleBean> call, Response<ArticleBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        ArticleBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<ArticleBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void getVideoHistoryListByDevIdAndTime(String devId,String userId, String carId, String startTime, String endTime,
                                                         String pageSize,
                                                         String pageNumber, Callback<VideoSourceBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getVideoHistoryListByDevIdAndTime(devId, userId,carId,startTime, endTime, pageSize, pageNumber)
                .enqueue(new retrofit2.Callback<VideoSourceBean>() {
                    @Override
                    public void onResponse(Call<VideoSourceBean> call, Response<VideoSourceBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        VideoSourceBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<VideoSourceBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void getUserBySUid(String suid, Callback<UserBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getUserBySUid(suid)
                .enqueue(new retrofit2.Callback<UserBean>() {
                    @Override
                    public void onResponse(Call<UserBean> call, Response<UserBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        UserBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<UserBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void getVideoList(Callback<VIdeoListBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getMyMissionAndDeviceAndUserByTokenForAppVideo()
                .enqueue(new retrofit2.Callback<VIdeoListBean>() {
                    @Override
                    public void onResponse(Call<VIdeoListBean> call, Response<VIdeoListBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        VIdeoListBean renWuLeiBean = response.body();
                        callback.onResponse(renWuLeiBean);
                    }

                    @Override
                    public void onFailure(Call<VIdeoListBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void addEmegencyNotice(String name,
                                         String content,
                                         String status,
                                         String picUrl, Callback<BaseBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().addEmegencyNotice(name, content, status, picUrl)
                .enqueue(new retrofit2.Callback<BaseBean>() {
                    @Override
                    public void onResponse(Call<BaseBean> call, Response<BaseBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        BaseBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<BaseBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void removeEmegencyNotice(
            String id, Callback<BaseBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().removeEmegencyNotice(id)
                .enqueue(new retrofit2.Callback<BaseBean>() {
                    @Override
                    public void onResponse(Call<BaseBean> call, Response<BaseBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        BaseBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<BaseBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void addIntercomInfoByToken(String missionId,
                                              String intercomId,
                                              String name, Callback<BaseBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().addIntercomInfoByToken(missionId, intercomId, name)
                .enqueue(new retrofit2.Callback<BaseBean>() {
                    @Override
                    public void onResponse(Call<BaseBean> call, Response<BaseBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        BaseBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<BaseBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void selectIntercomInfoByTokenAndCaseId(String type,String missionId, Callback<DuijiangBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().selectIntercomInfoByTokenAndCaseId(type,missionId)
                .enqueue(new retrofit2.Callback<DuijiangBean>() {
                    @Override
                    public void onResponse(Call<DuijiangBean> call, Response<DuijiangBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        DuijiangBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<DuijiangBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public static void addKeyNodesByToken(String caseId,
                                          String missionId,
                                          String longitude,
                                          String latitude,
                                          String description,
                                          Callback<BaseBean> callback) {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().addKeyNodesByToken(caseId, missionId, longitude, latitude, description)
                .enqueue(new retrofit2.Callback<BaseBean>() {
                    @Override
                    public void onResponse(Call<BaseBean> call, Response<BaseBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(AppEnvirment.getApplication());
                            return;
                        }
                        BaseBean baseBean = response.body();
                        callback.onResponse(baseBean);
                    }

                    @Override
                    public void onFailure(Call<BaseBean> call, Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }

    public interface Callback<T> {

        void onResponse(T call);

    }


}
