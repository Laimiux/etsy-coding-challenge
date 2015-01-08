package com.example.etsysearch.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.etsysearch.R;
import com.example.etsysearch.data.model.SearchResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by laimiux on 1/7/15.
 */
public class SearchResultAdapter extends BaseAdapter {
    final private LayoutInflater inflater;
    final private Picasso picasso;
    private List<SearchResult> results;

    final private BehaviorSubject<Integer> arraySizeBehaviorSubject = BehaviorSubject.create(0);

    public SearchResultAdapter(Context context, Picasso picasso) {
        inflater = LayoutInflater.from(context);
        this.picasso = picasso;
        results = new ArrayList<>();
    }


    public void setItems(Collection<? extends SearchResult> searchResults) {
        results.clear();
        results.addAll(searchResults);
        arraySizeBehaviorSubject.onNext(results.size());
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends SearchResult> searchResults) {
        results.addAll(searchResults);
        arraySizeBehaviorSubject.onNext(results.size());
        notifyDataSetChanged();
    }

    public void clear() {
        results.clear();
        arraySizeBehaviorSubject.onNext(0);
        notifyDataSetChanged();
    }


    public Observable<Integer> observeAdapterSize() {
        return arraySizeBehaviorSubject.asObservable();
    }

    @Override public int getCount() {
        return results.size();
    }

    @Override public SearchResult getItem(int position) {
        return results.get(position);
    }

    @Override public long getItemId(int position) {
        return getItem(position).getListingId();
    }

    @Override public boolean hasStableIds() {
        return true;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.search_result_list_item, parent, false);
        }

        ((SearchResultItemView) convertView).setItem(getItem(position), picasso);

        return convertView;
    }


}
