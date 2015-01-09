package com.example.etsysearch.data.api;

import com.example.etsysearch.data.model.SearchQuery;
import com.example.etsysearch.data.model.SearchResults;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;

/**
 * Poor man's in-memory cache that keeps last search results.
 */
public class CachedSearchService {
    final private EtsyService service;
    final private Map<SearchQuery, Observable<SearchResults>> requests;

    private CachedSearchService(EtsyService service) {
        this.service = service;
        requests = new HashMap<>();
    }


    /**
     * Not thread safe.
     */
    public Observable<SearchResults> search(SearchQuery searchQuery) {
        if (requests.containsKey(searchQuery)) {
            return requests.get(searchQuery);
        }


        // We only keep for a single query results
        if (searchQuery.getPage() == 1) {
            requests.clear();
        }

        final Observable<SearchResults> searchRequest = service.search(searchQuery.getQueryMap()).cache();
        requests.put(searchQuery, searchRequest);


        return searchRequest;
    }


    private static CachedSearchService cachedSearchService;

    public static CachedSearchService getCachedSearchService(EtsyService etsyService) {
        if (cachedSearchService == null) {
            synchronized (CachedSearchService.class) {
                if (cachedSearchService == null) {
                    cachedSearchService = new CachedSearchService(etsyService);
                }
            }
        }

        return cachedSearchService;
    }
}
