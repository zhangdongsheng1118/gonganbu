package com.zuozhan.app.net;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** @Author d @Time 2018/9/7 @Version 1.0 */
public class CommonInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request oldRequest = chain.request();

//        HttpUrl.Builder authorizedUrlBuilder =
//                oldRequest
//                        .url()
//                        .newBuilder()
//                        .scheme(oldRequest.url().scheme())
//                        .host(oldRequest.url().host())
//                        .addQueryParameter(
//                                "isEn",
//                                "zh".equals(ChangeLanguageManager.getLanguage()) ? "0" : "1");
//
//        // 新的请求
//        Request newRequest =
//                oldRequest
//                        .newBuilder()
//                        .method(oldRequest.method(), oldRequest.body())
//                        .url(authorizedUrlBuilder.build())
//                        .build();

//        return chain.proceed(newRequest);
        return chain.proceed(oldRequest);
    }
}
