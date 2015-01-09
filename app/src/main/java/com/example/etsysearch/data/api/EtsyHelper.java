package com.example.etsysearch.data.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

/**
 * Note: move to dagger modules when restructuring.
 */
public class EtsyHelper {
    public static final String ENDPOINT = "https://api.etsy.com/v2";
    public static final String ETSY_KEY = "liwecjs0c3ssk6let4p1wqt9";

    // Thread-safe lazy initialization
    private static class Holder {
       private static EtsyHelper helper = new EtsyHelper();
    }

    // Adapter that creates REST service instances
    final private RestAdapter adapter;

    private EtsyHelper() {
        // Create our Converter
        JacksonConverter jacksonConverter = new JacksonConverter(new ObjectMapper());

        adapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setConverter(jacksonConverter)
                .setEndpoint(ENDPOINT)
                .build();
    }


    public static RestAdapter getAdapter() {
        return Holder.helper.adapter;
    }

    public static EtsyService getEtsyService() {
        return Holder.helper.adapter.create(EtsyService.class);
    }
}
