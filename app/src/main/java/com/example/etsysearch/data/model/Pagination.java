package com.example.etsysearch.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pagination {

    @JsonProperty("next_page")
    private Integer nextPage;

    public Pagination() {
    }

    public Integer getNextPage() {
        return nextPage;
    }
}
