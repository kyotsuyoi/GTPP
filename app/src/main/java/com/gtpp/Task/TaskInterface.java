package com.gtpp.Task;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface TaskInterface {

    @GET("GTPP/Task.php")
    Call<JsonObject> GetTask(
            @Query("AUTH") String auth,
            @Query("mobile") int mobile,
            @Query("task_id") int task_id
    );

    @POST("GTPP/TaskItem.php")
    Call<JsonObject> PostTaskItem(
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );

    @PUT("GTPP/TaskItem.php")
    Call<JsonObject> PutTaskItem(
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );

    @DELETE("GTPP/TaskItem.php")
    Call<JsonObject> DeleteTaskItem(
            @Query("AUTH") String auth,
            @Query("task_id") int task_id,
            @Query("id") int id
    );

    @PUT("GTPP/TaskState.php")
    Call<JsonObject> PutTaskState(
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );

    @PUT("GTPP/Task.php")
    Call<JsonObject> PutDays(
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );

    @GET("GTPP/Task_User.php")
    Call<JsonObject> GetTaskUser(
            @Query("AUTH") String auth,
            @Query("list_user") int list_user,
            @Query("task_id") int task_id
    );

    @PUT("GTPP/Task_User.php")
    Call<JsonObject> PutTaskUser(
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );

    @POST("GTPP/TaskCompany.php")
    Call<JsonObject> PostComShoDepSub(
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );

    @POST("GTPP/TaskHistoric.php")
    Call<JsonObject> PostHistoric(
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );

    @GET("GTPP/TaskHistoric.php")
    Call<JsonObject> GetHistoric(
            @Query("AUTH") String auth,
            @Query("task_id") int task_id
    );
}
