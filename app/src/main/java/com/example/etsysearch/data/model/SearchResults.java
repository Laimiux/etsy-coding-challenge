package com.example.etsysearch.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // Do not care about the whole response object
public class SearchResults {
    @JsonProperty("results")
    private List<SearchResult> results;

    @JsonProperty("pagination")
    private Pagination pagination;

    // Empty constructor for jackson json deserialization
    public SearchResults() {
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
