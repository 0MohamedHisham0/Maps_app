package com.m.hisham.maps_app.network.remote;

import com.m.hisham.maps_app.models.Restaurant_Model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Remote_Dao {
    @GET("/maps/api/place/nearbysearch/json")
    Call<Restaurant_Model> getRestaurant(
            @Query("keyword") String keyword,
            @Query("location") String location,
            @Query("radius") String radius,
            @Query("type") String type,
            @Query("key") String key
    );
}
