package com.example.etsysearch.ui;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.etsysearch.R;
import com.example.etsysearch.data.api.EtsyHelper;
import com.example.etsysearch.data.api.EtsyService;
import com.example.etsysearch.data.model.SearchQuery;
import com.example.etsysearch.data.model.SearchResults;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Contains query view and list results.
 */
public class SearchContainer extends RelativeLayout {
    @InjectView(R.id.search_field) SearchField searchField;
    @InjectView(R.id.search_loading_indicator) View searchLoadingIndicator;
    @InjectView(R.id.search_result_list) ListView searchResultList;
    @InjectView(R.id.empty_search_list_view) TextView emptySearchTextView;

    final private EtsyService etsyService;
    final private SearchResultAdapter searchResultAdapter;

    // Mutable state
    private boolean isLoading;
    private SearchQuery lastQuery;

    // Observables
    private BehaviorSubject<Boolean> observeLoading = BehaviorSubject.create(false);

    // Subscriptions
    private Subscription searchClickSubscription;
    private Subscription searchNetworkRequestSubscription;
    private Subscription emptyIndicatorVisibilitySubscription;

    public SearchContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        etsyService = EtsyHelper.getEtsyService();
        searchResultAdapter = new SearchResultAdapter(context, Picasso.with(context));
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);

        searchResultList.setAdapter(searchResultAdapter);

        // Load new items when at the bottom of the page.
        searchResultList.setOnScrollListener(new AbsListView.OnScrollListener() {
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

        // just in case not to leak memory
        searchClickSubscription.unsubscribe();
        searchClickSubscription = null;

        emptyIndicatorVisibilitySubscription.unsubscribe();
        emptyIndicatorVisibilitySubscription = null;
    }

    private void loadNextPage() {
        // TODO check that there is next page
        if (lastQuery != null && !isLoading) {
            Log.d("SearchContainer", "loadNextPage()");

            final SearchQuery searchQuery = lastQuery.setPage(lastQuery.getPage() + 1);
            performSearch(searchQuery);
        }
    }

    private void performSearch(final SearchQuery searchQuery) {
        Log.d("SearchContainer", "performSearch() -> " + searchQuery);

        setLoading(true);

        final boolean isNewSearchTerm = searchQuery.getPage() == 1;
        if (isNewSearchTerm) {
            searchField.setEnabled(false);
            searchLoadingIndicator.setVisibility(View.VISIBLE);
            searchResultAdapter.clear();
        }

        clearNetworkRequest();

        // We assert that no other search request is going on at the same time
        searchNetworkRequestSubscription = etsyService.search(searchQuery.getQueryMap())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SearchResults>() {
                    @Override public void call(SearchResults searchResults) {
                        if (isNewSearchTerm) { // New search query
                            searchField.setEnabled(true);
                            searchResultAdapter.setItems(searchResults.getResults());
                            searchLoadingIndicator.setVisibility(View.GONE);
                        } else { // new page of search query
                            searchResultAdapter.addAll(searchResults.getResults());
                        }

                        lastQuery = searchQuery;

                        setLoading(false);
                    }
                }, new Action1<Throwable>() {
                    @Override public void call(Throwable throwable) {
                        if (isNewSearchTerm) {
                            searchLoadingIndicator.setVisibility(View.GONE);
                            searchField.setEnabled(true);
                        }

                        setLoading(false);
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
        observeLoading.onNext(isLoading);
    }


    private void clearNetworkRequest() {
        if(searchNetworkRequestSubscription != null) {
            searchNetworkRequestSubscription.unsubscribe();
            searchNetworkRequestSubscription = null;
        }
    }

    @Override protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}
