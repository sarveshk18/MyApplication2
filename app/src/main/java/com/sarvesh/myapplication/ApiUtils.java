package com.sarvesh.myapplication;


public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "https://androsecure.herokuapp.com/";
    // public static final String BASE_URL ="http://192.168.42.197:5000";
    public static ApiInterface getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(ApiInterface.class);
    }
}