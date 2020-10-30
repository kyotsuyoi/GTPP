package com.gtpp.CommonClasses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClientForImage {

    public static final String BASE_URL = "http://XXX.XXX.XXX.XXX:71/GLOBAL/Controller/";
    //public static final String BASE_URL = "http://192.168.0.99:71/GLOBAL/Controller/";
    public static Retrofit retrofit;

    public  static Retrofit getApiClient(){

        if (retrofit == null){
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }

}
