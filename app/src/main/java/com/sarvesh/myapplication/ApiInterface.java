package com.sarvesh.myapplication;


import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {

    @Headers("Content-type: application/json")
    @POST("/sendreq")
    Call<ResponseBody> getLocation(@Body RequestBody requestBody);

}