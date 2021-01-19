package com.gtpp.Main;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface MainInterface {

    @GET("GTPP/Task.php")
    Call<JsonObject> GetTask(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("user_id") int user_id,
            @Query("mobile") int mobile,
            @Query("administrator") int admin_visualization
    );

    @POST("GTPP/Task.php")
    Call<JsonObject> PostTask(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("mobile") int mobile,
            @Body JsonObject jsonObject
    );

    @PUT("GTPP/Task.php")
    Call<JsonObject> PutTask(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );

    @GET("CCPP/Company.php")
    Call<JsonObject> GetCompany(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth
    );

    @GET("CCPP/Shop.php")
    Call<JsonObject> GetShop(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("company_id") int company_id
    );

    @GET("CCPP/Department.php")
    Call<JsonObject> GetDepartment(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("company_id") int company_id,
            @Query("shop_id") int shop_id
    );

    @GET("CCPP/Department.php")
    Call<JsonObject> GetDepartmentCheck(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("company_id") int company_id,
            @Query("shop_id") int shop_id,
            @Query("task_id") int task_id
    );

    @GET("GTPP/TaskState.php")
    Call<JsonObject> GetTaskState(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth
    );

    @DELETE("GTPP/Task.php")
    Call<JsonObject> DeleteTask(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("id") int id
    );

    @GET("CCPP/Employee.php")
    Call<JsonObject> GetEmployee(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("id") int id
    );

    @GET("CCPP/EmployeePhoto.php")
    Call<JsonObject> GetEmployeePhoto(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("id") int id
    );

    @GET("GTPP/Score.php")
    Call<JsonObject> GetScore(
            @Query("app_id") int app_id,
            @Query("AUTH") String auth,
            @Query("user_id") int user_id,
            @Query("all") String all
    );
}
