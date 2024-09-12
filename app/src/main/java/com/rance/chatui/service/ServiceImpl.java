package com.rance.chatui.service;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceImpl implements IService {
    private WebService webService;
    private Retrofit retrofit;
    private OkHttpClient client;
    private static final String SSL_CONTEXT_TLS = "TLS";
    static final private int TIME_OUT_THRESHOLD = 30;

    @Override
    public void initialize() {
        String endPoint = "http://127.0.0.1:11434";
        OkHttpClient okHttpClient = getOkHttpClient(TIME_OUT_THRESHOLD);
        retrofit = new Retrofit.Builder()
                .baseUrl(endPoint)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        webService = retrofit.create(WebService.class);
    }

    @Override
    public void sendRequest(String question, IRequestCallback callback) {
        RequestBody requestBody = new RequestBody(question);
        Call<ResponseBody> call = webService.sendRequest(requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    callback.onSuccess(response.body().response);
                } else {
                    callback.onFailed(response.message());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailed(t.getMessage());
            }
        });
    }

    private OkHttpClient getOkHttpClient(int timeout) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts())
                    .build();

        }

        return client;
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance(SSL_CONTEXT_TLS);
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            return ssfFactory;
        }

        return ssfFactory;
    }

    static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
