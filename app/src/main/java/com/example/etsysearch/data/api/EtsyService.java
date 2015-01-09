package com.example.etsysearch.data.api;

import com.example.etsysearch.data.model.SearchResults;

import java.util.Map;

import retrofit.http.GET;
import retrofit.http.QueryMap;
import rx.Observable;

public interface EtsyService {

    @GET("/listings/active")
    Observable<SearchResults> search(@QueryMap Map<String, Object> queryMap);
}
