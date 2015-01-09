package com.example.etsysearch.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class SearchQuery implements Parcelable {
    // TODO move api key decoration out of this class
    private static final String ETSY_KEY = "liwecjs0c3ssk6let4p1wqt9";
    private static final String API_KEY = "api_key";
    private static final String INCLUDES = "includes";
    public static final String MAIN_IMAGE = "MainImage";
    private static final String KEYWORDS = "keywords";
    private static final String PAGE = "page";


    final private String keywords;
    final private Integer page;


    public SearchQuery(String searchTerms) {
        this(searchTerms, 1);
    }

    public SearchQuery(String keywords, Integer page) {
        this.keywords = keywords;
        this.page = page;
    }


    protected SearchQuery(Parcel in) {
        keywords = in.readString();
        page = in.readInt();
    }


    public SearchQuery getNextPageQuery() {
        return new SearchQuery(keywords, page+1);
    }

    public SearchQuery withPage(int page) {
        return new SearchQuery(keywords, page);
    }

    public Map<String, Object> getQueryMap() {
        // todo doesn't really belong here
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(API_KEY, ETSY_KEY);
        queryMap.put(INCLUDES, MAIN_IMAGE);

        queryMap.put(PAGE, page);
        queryMap.put(KEYWORDS, keywords);
        return queryMap;
    }

    public Integer getPage() {
        return page;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(keywords);
        dest.writeInt(page);
    }


    //required field that makes Parcelables from a Parcel
    public static final Parcelable.Creator<SearchQuery> CREATOR =
            new Parcelable.Creator<SearchQuery>() {
                public SearchQuery createFromParcel(Parcel in) {
                    return new SearchQuery(in);
                }
                public SearchQuery[] newArray(int size) {
                    return new SearchQuery[size];
                }
            };
}
