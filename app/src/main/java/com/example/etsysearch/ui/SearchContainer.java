package com.example.etsysearch.ui;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.example.etsysearch.R;
import com.example.etsysearch.data.api.CachedSearchService;
import com.example.etsysearch.data.api.EtsyHelper;
import com.example.etsysearch.data.model.SearchQuery;
import com.example.etsysearch.data.model.SearchResult;
import com.example.etsysearch.data.model.SearchResults;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Contains query view and result view.
 */
public class SearchContainer extends RelativeLayout {
    @InjectView(R.id.search_field) SearchField searchField;
    @InjectView(R.id.search_loading_indicator) View searchLoadingIndicator;
    @InjectView(R.id.search_result_list) StaggeredGridView searchResultGrid;
    @InjectView(R.id.empty_search_list_view) TextView emptySearchTextView;

    final private CachedSearchService etsyService;
    final private SearchResultAdapter searchResultAdapter;

    // Mutable state
    private boolean hasNextPage;
    private SearchQuery lastQuery;
    private SearchQuery currentlyLoading;

    // Observables
    private BehaviorSubject<Boolean> observeLoading = BehaviorSubject.create(false);

    // Subscriptions
    private Subscription searchClickSubscription;
    private Subscription searchNetworkRequestSubscription;
    private Subscription emptyIndicatorVisibilitySubscription;
    private Subscription restoreStateSubscription;

    public SearchContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        etsyService = CachedSearchService.getCachedSearchService(EtsyHelper.getEtsyService());
        searchResultAdapter = new SearchResultAdapter(context, Picasso.with(context));
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);

        searchResultGrid.setAdapter(searchResultAdapter);

        // Load new items when at the bottom of the page.
        searchResultGrid.setOnScrollListener(new AbsListView.OnScrollListener() {
            // todo decide what's the number of items should be
            int itemsLeft = 8; // Items left before loading more

            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount - firstVisibleItem <= itemsLeft) {
                    loadNextPage();
                }

            }
        });
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Need to test if you need to unsubscribe with onDetachFromWindow,
        // if not, then move this to onFinishInflate
        searchClickSubscription = searchField.observeSearch().subscribe(new Action1<SearchQuery>() {
            @Override public void call(SearchQuery searchQuery) {
                performSearch(searchQuery);
            }
        });

        emptyIndicatorVisibilitySubscription = Observable.combineLatest(searchResultAdapter.observeAdapterSize(), observeLoading.asObservable(), new Func2<Integer, Boolean, Boolean>() {
            @Override public Boolean call(Integer size, Boolean isLoading) {
                return size == 0 && !isLoading;
            }
        }).subscribe(new Action1<Boolean>() {
            @Override public void call(Boolean shouldShow) {
                if (shouldShow) {
                    final int msgID = lastQuery == null ? R.string.search_something : R.string.no_results_found;
                    emptySearchTextView.setText(msgID);
                    emptySearchTextView.setVisibility(View.VISIBLE);
                } else {
                    emptySearchTextView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        clearNetworkRequest();

        if(restoreStateSubscription != null) {
            restoreStateSubscription.unsubscribe();
            restoreStateSubscription = null;
        }

        // just in case not to leak memory
        searchClickSubscription.unsubscribe();
        searchClickSubscription = null;

        emptyIndicatorVisibilitySubscription.unsubscribe();
        emptyIndicatorVisibilitySubscription = null;
    }

    private void loadNextPage() {
        if (lastQuery != null && hasNextPage && currentlyLoading == null) {
            Log.d("SearchContainer", "loadNextPage()");

            final SearchQuery searchQuery = lastQuery.getNextPageQuery();
            performSearch(searchQuery);
        }
    }

    private void performSearch(final SearchQuery searchQuery) {
        Log.d("SearchContainer", "performSearch() -> " + searchQuery);

        setLoading(searchQuery);

        final boolean isNewSearchTerm = searchQuery.getPage() == 1;
        if (isNewSearchTerm) {
            searchField.setEnabled(false);
            searchLoadingIndicator.setVisibility(View.VISIBLE);
            searchResultAdapter.clear();

            // Reset last query
            lastQuery = null;
        }

        clearNetworkRequest();

        // We assert that no other search request is going on at the same time
        searchNetworkRequestSubscription = getSearchResultsObservable(searchQuery)
                .subscribe(new Action1<SearchResults>() {
                    @Override public void call(SearchResults searchResults) {

                        final List<SearchResult> results = searchResults.getResults();
                        if (isNewSearchTerm) { // New search query
                            searchField.setEnabled(true);
                            searchResultAdapter.setItems(results);
                            searchLoadingIndicator.setVisibility(View.GONE);
                        } else { // new page of search query
                            searchResultAdapter.addAll(results);
                        }

                        hasNextPage = searchResults.getPagination().getNextPage() != null;
                        lastQuery = searchQuery;

                        setLoading(null);
                    }
                }, new Action1<Throwable>() {
                    @Override public void call(Throwable throwable) {
                        if (isNewSearchTerm) {
                            searchLoadingIndicator.setVisibility(View.GONE);
                            searchField.setEnabled(true);
                        }

                        setLoading(null);
                    }
                });
    }

    private Observable<SearchResults> getSearchResultsObservable(SearchQuery searchQuery) {
        return etsyService.search(searchQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Set's currently loading query
     * Pass null to indicate that loading has stopped
     */
    private void setLoading(SearchQuery currentlyLoading) {
        this.currentlyLoading = currentlyLoading;
        observeLoading.onNext(currentlyLoading != null);
    }


    private void clearNetworkRequest() {
        if (searchNetworkRequestSubscription != null) {
            searchNetworkRequestSubscription.unsubscribe();
            searchNetworkRequestSubscription = null;
        }
    }

    @Override protected Parcelable onSaveInstanceState() {
        final Parcelable parcelable = searchResultGrid.onSaveInstanceState();
        return new SavedState(super.onSaveInstanceState(), hasNextPage, lastQuery, currentlyLoading, parcelable);
    }

    @Override protected void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        hasNextPage = ss.hasNextPage;
        lastQuery = ss.lastQuery;

        if (lastQuery != null) {
            // Restore old list
            restoreStateSubscription = Observable.range(1, lastQuery.getPage())
                    .flatMap(new Func1<Integer, Observable<SearchResults>>() {
                        @Override public Observable<SearchResults> call(Integer page) {
                            return getSearchResultsObservable(lastQuery.withPage(page));
                        }
                    }).map(new Func1<SearchResults, List<SearchResult>>() {
                        @Override public List<SearchResult> call(SearchResults searchResults) {
                            return searchResults.getResults();
                        }
                    }).reduce(new ArrayList<SearchResult>(), new Func2<ArrayList<SearchResult>, List<SearchResult>, ArrayList<SearchResult>>() {
                        @Override
                        public ArrayList<SearchResult> call(ArrayList<SearchResult> concatResults, List<SearchResult> searchResults) {
                            concatResults.addAll(searchResults);
                            return concatResults;
                        }
                    }).subscribe(new Action1<ArrayList<SearchResult>>() {
                        @Override public void call(ArrayList<SearchResult> searchResults) {
                            searchResultAdapter.setItems(searchResults);
                            searchResultGrid.onRestoreInstanceState(ss.gridState);

                            // Restore last loading thing
                            if(ss.currentlyLoading != null) {
                                performSearch(ss.currentlyLoading);
                            }
                        }
                    });
        } else {
            // Restore last loading thing
            if(ss.currentlyLoading != null) {
                performSearch(ss.currentlyLoading);
            }
        }
    }


    static class SavedState extends BaseSavedState {
        final boolean hasNextPage;
        final SearchQuery lastQuery;
        final SearchQuery currentlyLoading;
        final Parcelable gridState;

        public SavedState(Parcel source) {
            super(source);
            hasNextPage = source.readInt() == 1;

            final ClassLoader classLoader = SearchQuery.class.getClassLoader();
            lastQuery = source.readParcelable(classLoader);
            currentlyLoading = source.readParcelable(classLoader);

            gridState = source.readParcelable(null);
        }

        public SavedState(Parcelable superState, boolean hasNextPage, SearchQuery lastQuery, SearchQuery currentlyLoading, Parcelable gridState) {
            super(superState);
            this.hasNextPage = hasNextPage;
            this.lastQuery = lastQuery;
            this.currentlyLoading = currentlyLoading;
            this.gridState = gridState;
        }

        @Override public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(hasNextPage ? 1 : 0);
            dest.writeParcelable(lastQuery, flags);
            dest.writeParcelable(currentlyLoading, flags);
            dest.writeParcelable(gridState, flags);
        }
    }

    public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
        @Override public SavedState createFromParcel(Parcel source) {
            return new SavedState(source);
        }

        @Override public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };
}
