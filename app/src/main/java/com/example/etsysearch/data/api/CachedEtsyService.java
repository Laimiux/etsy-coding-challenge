package com.example.etsysearch.data.api;

import com.example.etsysearch.data.model.SearchQuery;
import com.example.etsysearch.data.model.SearchResults;

import rx.Observable;

public class CachedEtsyService {
    final private EtsyService service;

//    private Map<SearchQuery, Observable<>>

    private CachedEtsyService(EtsyService service) {
        this.service = service;
    }


    public Observable<SearchResults> search(SearchQuery searchQuery) {
        return service.search(searchQuery.getQueryMap());
    }


    private static CachedEtsyService cachedEtsyService;

    public static CachedEtsyService getCachedEtsyService(EtsyService etsyService) {
        if (cachedEtsyService == null) {
            synchronized (CachedEtsyService.class) {
                if (cachedEtsyService == null) {
                    cachedEtsyService = new CachedEtsyService(etsyService);
                }
            }
        }

        return cachedEtsyService;
    }
}
