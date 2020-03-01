package com.yulin.trackingevent.rest;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Ivan Y. on 2020-02-24.
 */

public interface PostEventService {
    @FormUrlEncoded
    @POST("hexa/")
    Call<String> postEvent(@Field("key") String id,
                           @Field("event") String event,
                           @Field("client_timestamp") String timestamp,
                           @Field("geo") String latLng,
                           @Field("device_version") int deviceVersion);
}
