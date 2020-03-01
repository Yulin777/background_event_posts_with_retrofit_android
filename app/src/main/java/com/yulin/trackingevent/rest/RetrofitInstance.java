package com.yulin.trackingevent.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ivan Y. on 2020-02-24.
 */

public class RetrofitInstance {
    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            instance = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return instance;
    }

}
