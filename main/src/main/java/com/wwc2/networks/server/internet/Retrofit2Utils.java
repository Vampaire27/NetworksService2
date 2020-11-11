package com.wwc2.networks.server.internet;

import android.content.Context;

import com.wwc2.networks.server.provider.sharedpreference.SPUtils;
import com.wwc2.networks.server.utils.Config;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit2Utils {

    private Retrofit retrofit = null;
    private static final long TIMEOUT = 60;
    private static Context context = null;
    private static Retrofit2Utils retrofit2Utils = null;
    private static OkHttpClient httpClient = null;

    private Retrofit2Utils(){}

    public static Retrofit2Utils newInstance(Context con) {
        if (retrofit2Utils == null) {
            retrofit2Utils = new Retrofit2Utils();
            init();
        }
        context = con;
        return retrofit2Utils;
    }

    private static void init(){
        if (Config.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addNetworkInterceptor(interceptor)
                .retryOnConnectionFailure(true)
                .build();
        }
    }

    private void setRetrofit(String url){
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build();
    }

    public Retrofit getProtocolRetrofit(String url){
        if(url != null){
            setRetrofit(url);
        }else{
            String def_url = (String) SPUtils.get(context,
                    Config.SYS_PROTOCOL, Config.SYS_PROTOCOL_URL);
            setRetrofit(def_url);
        }
        return retrofit;
    }

    public Retrofit getInterfaceRetrofit(String url){
        if(url != null){
            setRetrofit(url);
        }else{
            String def_url = (String) SPUtils.get(context,
                    Config.SYS_INTERFACE, Config.SYS_INTERFACE_URL);
            setRetrofit(def_url);
        }
        return retrofit;
    }

    public Retrofit getWeatherRetrofit(String url){
        if(url != null){
            setRetrofit(url);
        }else{
            String def_url = (String) SPUtils.get(context,
                    Config.APP_WEATHER, Config.APP_WEATHER_URL);
            setRetrofit(def_url);
        }
        return retrofit;
    }

    public Retrofit getJiaShenRetrofit(){
        setRetrofit(Config.JS_URL);
        return retrofit;
    }

    public Retrofit getZWRetrofit(){
        setRetrofit(Config.ZW_URL);
        return retrofit;
    }
}
