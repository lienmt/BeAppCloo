package com.beappclootp.lmt.network;

import com.beappclootp.lmt.model.BiClooData;
import com.beappclootp.lmt.model.directions.DirectionResults;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by lien.muguercia on 31/03/2018.
 */
public interface NetworkService {

    @GET("stations?contract=Nantes&apiKey=b64b7c141064b8a54cf32e115d9307aa74e6fd11")
    Call<List<BiClooData>> getAllData();

    @GET("/maps/api/directions/json?sensor=false")
    Call<DirectionResults> getGoogleDirections(@Query("origin") String origin, @Query("destination") String destination, @Query("mode") String mode);

}
