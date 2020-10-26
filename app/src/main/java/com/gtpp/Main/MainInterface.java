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
            @Query("AUTH") String auth,
            @Query("user_id") int user_id,
            @Query("mobile") int mobile,
            @Query("administrator") int admin_visualization
    );

    @POST("GTPP/Task.php")
    Call<JsonObject> PostTask(
            @Query("AUTH") String auth,
            @Query("mobile") int mobile,
            @Body JsonObject jsonObject
    );

    @PUT("GTPP/Task.php")
    Call<JsonObject> PutTask(
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );

    @GET("Company.php")
    Call<JsonObject> GetCompany(
            @Query("AUTH") String auth
    );

    @GET("Shop.php")
    Call<JsonObject> GetShop(
            @Query("AUTH") String auth,
            @Query("company_id") int company_id
    );

    @GET("Departament.php")
    Call<JsonObject> GetDepartment(
            @Query("AUTH") String auth,
            @Query("company_id") int company_id,
            @Query("shop_id") int shop_id
    );

    @GET("Departament.php")
    Call<JsonObject> GetDepartmentCheck(
            @Query("AUTH") String auth,
            @Query("company_id") int company_id,
            @Query("shop_id") int shop_id,
            @Query("task_id") int task_id
    );

    @GET("GTPP/TaskState.php")
    Call<JsonObject> GetTaskState(
            @Query("AUTH") String auth
    );

    @DELETE("GTPP/Task.php")
    Call<JsonObject> DeleteTask(
            @Query("AUTH") String auth,
            @Query("id") int id
    );

    @GET("Employee.php")
    Call<JsonObject> GetEmployee(
            @Query("AUTH") String auth,
            @Query("id") int id
    );

    @GET("EmployeePhoto.php")
    Call<JsonObject> GetEmployeePhoto(
            @Query("AUTH") String auth,
            @Query("id") int id
    );
}
