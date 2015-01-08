package com.example.etsysearch.data.model;

import java.util.HashMap;
import java.util.Map;

public class SearchQuery {
    // TODO move api key decoration out of this class
    private static final String ETSY_KEY = "liwecjs0c3ssk6let4p1wqt9";
    private static final String API_KEY = "api_key";
    private static final String INCLUDES = "includes";
    public static final String MAIN_IMAGE = "MainImage";
    private static final String KEYWORDS = "keywords";
    private static final String PAGE = "page";


    final private Map<String, Object> queryMap = new HashMap<>();

    public SearchQuery(String searchTerms) {
        queryMap.put(KEYWORDS, searchTerms);
    }

    private SearchQuery(Map<String, Object> init) {
        queryMap.putAll(init);
    }

    public SearchQuery setPage(Integer page) {
        if(page < 1) {
            throw new IllegalStateException("page cannot be less than 1");
        }

        final SearchQuery searchQuery = new SearchQuery(queryMap);
        searchQuery.queryMap.put(PAGE, page);

        return searchQuery;
    }

    public Map<String, Object> getQueryMap() {
        // todo doesn't really belong here
        queryMap.put(API_KEY, ETSY_KEY);
        queryMap.put(INCLUDES, MAIN_IMAGE);
        return queryMap;
    }

    public Integer getPage() {
        final Object o = queryMap.get(PAGE);
        if(o == null) {
            return 1;
        }

        return (Integer) o;
    }
}
