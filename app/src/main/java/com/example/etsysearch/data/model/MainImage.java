package com.example.etsysearch.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by laimiux on 1/7/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // Do not care about the whole response object
public class MainImage {

    @JsonProperty("url_75x75")
    private String url75;

    @JsonProperty("url_170x135")
    private String url170;

    @JsonProperty("url_570xN")
    private String url570;

    @JsonProperty("url_fullxfull")
    private String urlFull;


    public MainImage() {
    }

    public String getUrl75() {
        return url75;
    }

    public String getUrl170() {
        return url170;
    }

    public String getUrl570() {
        return url570;
    }

    public String getUrlFull() {
        return urlFull;
    }
}
