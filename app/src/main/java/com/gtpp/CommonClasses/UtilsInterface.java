package com.gtpp.CommonClasses;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface UtilsInterface {

    @PUT("GTPP/TaskUtils.php")
    Call<JsonObject> PutGenericField(
            @Query("AUTH") String auth,
            @Body JsonObject jsonObject
    );
}
