package com.rance.chatui.service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface WebService {
    @POST("/api/generate")
    Call<ResponseBody> sendRequest(@Body RequestBody request);
}
