package com.gtpp.CommonClasses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String BASE_URL = "http://187.35.128.157:71/GLOBAL/Controller/";
    //public static final String BASE_URL = "http://192.168.0.99:71/GLOBAL/Controller/";
    public static Retrofit retrofit;

    public  static Retrofit getApiClient(){

        if (retrofit == null){
            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }

}
