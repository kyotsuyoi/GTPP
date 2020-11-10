package com.gtpp.Message;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface MessageInterface {

    @GET("GTPP/Message.php")
    Call<JsonObject> GetMessage(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("task_id") int task_id
    );

    @DELETE("GTPP/Message.php")
    Call<JsonObject> DeleteMessage(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("id") int id,
            @Query("task_id") int task_id
    );

    @GET("GTPP/Message.php")
    Call<JsonObject> GetMessageImage(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("id") int id
    );

    @POST("GTPP/Message.php")
    Call<JsonObject> PostMessage(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );
}
