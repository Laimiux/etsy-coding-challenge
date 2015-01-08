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

    private static class Holder {
       private static EtsyHelper helper = new EtsyHelper();
    }


    private RestAdapter adapter;


    private EtsyHelper() {
        // Create our Converter
        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        JacksonConverter jacksonConverter = new JacksonConverter(objectMapper);

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
