package com.beappclootp.lmt.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by lien.muguercia on 31/03/2018.
 */
public class NetServiceClient {

    private static Retrofit retrofit;
    private static final String BASE_URL_DATA = "https://api.jcdecaux.com/vls/v1/";
    private static final String BASE_URL_GOOGLE_DIRECTIONS = "http://maps.googleapis.com/";

    public static Retrofit setupClient(boolean loadData) {
        //if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(loadData ? BASE_URL_DATA : BASE_URL_GOOGLE_DIRECTIONS)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        //}

        return retrofit;
    }


}
