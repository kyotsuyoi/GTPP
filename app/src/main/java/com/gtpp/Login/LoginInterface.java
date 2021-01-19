package com.gtpp.Login;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoginInterface {

    @GET("CCPP/AppVersion.php")
    Call<JsonObject> GetAppVersion(
            @Query("id") int id
    );

    @POST("CCPP/Login.php?login")
    Call<JsonObject> PostLogin(
            @Query("app_id") int app_id,
            @Body JsonObject jsonObject
    );
}
