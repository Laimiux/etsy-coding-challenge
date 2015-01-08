package com.example.etsysearch.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) // Do not care about the whole response object
public class SearchResult {
    @JsonProperty("listing_id")
    private long listingId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("MainImage")
    private MainImage mainImage;

    public SearchResult() {
    }

    public String getTitle() {
        return title;
    }

    public long getListingId() {
        return listingId;
    }

    public String getDescription() {
        return description;
    }

    public MainImage getMainImage() {
        return mainImage;
    }

    @Override public String toString() {
        return "SearchResult{" +
                "description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", listingId=" + listingId +
                '}';
    }
}
