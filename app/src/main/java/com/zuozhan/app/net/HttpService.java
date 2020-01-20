package com.zuozhan.app.net;


import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.util.LogUtil;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 工具类调用 d 20180824
 */
public class HttpService<T> {

    private T questInterface;

    private HttpService(T api) {
        this.questInterface = api;
    }

    public T getServiceApi() {
        return (T) questInterface;
    }

    public static <T> HttpService<T> getApi(Class<T> tClass) {
        return new Builder()
                .addConverterFactory()
                .addCallAdapterFactory()
                .build(tClass);
    }

    public static <T> HttpService<T> getApiToHeader(Class<T> tClass) {
        return new Builder(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("Authorization", AppEnvirment.getToken())
                        .build();
                return chain.proceed(request);
            }
        })
                .addConverterFactory()
                .addCallAdapterFactory()
                .build(tClass);
    }

    public static <T> HttpService<T> getApi2(Class<T> tClass) {
        return new Builder(BaseIP.NET_BASE_URL)
                .addConverterFactory()
                .addCallAdapterFactory()
                .build(tClass);
    }

    /**
     * addInterceptor(new MyInterceptor()) .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
     * .readTimeout(TIMEOUT, TimeUnit.SECONDS) .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
     * .retryOnConnectionFailure(false)
     */
    public static class Builder<T> {
        Retrofit.Builder builder;

        OkHttpClient.Builder okBuild;

        private SSLSocketFactory  init(){
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)  {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)  {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                return sslSocketFactory;

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Builder() {
            okBuild = new OkHttpClient.Builder();
            if (BaseIP.isHttps){
                okBuild.sslSocketFactory(init());
                okBuild.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }
            if (LogUtil.isDebugMode()) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                okBuild.addInterceptor(httpLoggingInterceptor);
            }
            okBuild.addInterceptor(new CommonInterceptor());
            builder = new Retrofit.Builder().baseUrl(BaseIP.NET_BASE_URL).client(okBuild.build());
        }

        public Builder(Interceptor interceptor) {
            okBuild = new OkHttpClient.Builder();
            if (BaseIP.isHttps){
                okBuild.sslSocketFactory(init());
                okBuild.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }
            if (LogUtil.isDebugMode()) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                okBuild.addInterceptor(httpLoggingInterceptor);
            }
            okBuild.addInterceptor(new CommonInterceptor());
            okBuild.addInterceptor(interceptor);
            builder = new Retrofit.Builder().baseUrl(BaseIP.NET_BASE_URL).client(okBuild.build());
        }

        public Builder(String baseHttp) {
            okBuild = new OkHttpClient.Builder();
            if (BaseIP.isHttps){
                okBuild.sslSocketFactory(init());
                okBuild.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }
            if (LogUtil.isDebugMode()) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                okBuild.addInterceptor(httpLoggingInterceptor);
            }
            okBuild.addInterceptor(new CommonInterceptor());
            builder = new Retrofit.Builder().baseUrl(baseHttp).client(okBuild.build());
        }

        private Builder addConverterFactory() {
            builder.addConverterFactory(GsonConverterFactory.create());
            return this;
        }

        public Builder addCallAdapterFactory() {
            builder.addCallAdapterFactory(RxJavaCallAdapterFactory.create());
            return this;
        }

        public HttpService build(Class<T> apiClass) {
            T questInterface = builder.build().create(apiClass);
            return new HttpService(questInterface);
        }
    }

    //  public static class BuilderDownload {
    //    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    //    Retrofit.Builder builder =
    //        new Retrofit.Builder().baseUrl(Api.BASE_URL_TWO).client(okHttpClient);
    //
    //    public HttpUtils build() {
    //      InterfaceApi questInterface = builder.build().create(InterfaceApi.class);
    //      return myQusetUtils = new HttpUtils(questInterface);
    //    }
    //  }
}
