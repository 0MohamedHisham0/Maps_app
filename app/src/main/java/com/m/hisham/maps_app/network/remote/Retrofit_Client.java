package com.m.hisham.maps_app.network.remote;

import static com.m.hisham.maps_app.BuildConfig.GOOGLE_MAPS_API_KEY;

import com.m.hisham.maps_app.models.Restaurant_Model;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Query;

public class Retrofit_Client {
    private final String BASE_URL = "https://maps.googleapis.com";

    private final Remote_Dao remoteDB_dao;
    private static Retrofit_Client retrofit_client;

    public Retrofit_Client() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        remoteDB_dao = retrofit.create(Remote_Dao.class);
    }

    public static Retrofit_Client getInstance() {
        if (retrofit_client == null) {
            retrofit_client = new Retrofit_Client();
        }
        return retrofit_client;
    }

    public Call<Restaurant_Model> getRestaurants(String keyword, String location, String radius, String type) {
        return remoteDB_dao.getRestaurant(keyword, location, radius, type, GOOGLE_MAPS_API_KEY);
    }

}
