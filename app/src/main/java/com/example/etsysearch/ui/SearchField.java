package com.example.etsysearch.ui;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.etsysearch.R;
import com.example.etsysearch.data.model.SearchQuery;
import com.example.etsysearch.util.RxUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class SearchField extends RelativeLayout {
    @InjectView(R.id.search_query_view) EditText searchQueryView;
    @InjectView(R.id.search_button) Button searchButton;


    // Event helper
    final private PublishSubject<SearchQuery> searchEventPublishSubject = PublishSubject.create();

    // Subscriptions
    private Subscription searchQueryLengthSubscription;

    public SearchField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);

        searchButton.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                searchClick();
            }
        });
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        searchQueryLengthSubscription = RxUtils.observeEditTextCount(searchQueryView)
                .subscribe(new Action1<Integer>() {
                    @Override public void call(Integer integer) {
                        searchButton.setEnabled(integer > 0);
                    }
                });

        RxUtils.trackAction(searchQueryView, EditorInfo.IME_ACTION_SEARCH)
            .subscribe(new Action1<KeyEvent>() {
                @Override public void call(KeyEvent keyEvent) {
                    searchClick();
                }
            });
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Clear subscriptions
        searchQueryLengthSubscription.unsubscribe();
        searchQueryLengthSubscription = null;

    }

    private void searchClick() {
        CharSequence query = searchQueryView.getText();

        if (!TextUtils.isEmpty(query)) {
            Log.d("SearchField", "Search " + query);
            searchEventPublishSubject.onNext(new SearchQuery(query.toString()));
        } else {
            Toast.makeText(getContext(), R.string.enter_search_term, Toast.LENGTH_LONG).show();
        }
    }

    public Observable<SearchQuery> observeSearch() {
        return searchEventPublishSubject.asObservable();
    }

    @Override public void setEnabled(boolean enabled) {
        // Only enable toggles the button
        searchButton.setEnabled(enabled);
    }

    @Override protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), searchQueryView.getText().toString());
    }

    @Override protected void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());

        searchQueryView.setText(ss.searchTerm);
    }


    static class SavedState extends BaseSavedState {
        final String searchTerm;

        public SavedState(Parcel source) {
            super(source);
            searchTerm = source.readString();
        }

        public SavedState(Parcelable superState, String searchTerm) {
            super(superState);
            this.searchTerm = searchTerm;
        }


        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(searchTerm);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
