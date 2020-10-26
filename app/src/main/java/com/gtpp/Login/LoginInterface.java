package com.gtpp.Login;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface LoginInterface {

    @GET("GTPP/AppVersion.php")
    Call<JsonObject> GetAppVersion();

    @POST("Login.php?login")
    Call<JsonObject> PostLogin(
            @Body JsonObject jsonObject
    );
}
