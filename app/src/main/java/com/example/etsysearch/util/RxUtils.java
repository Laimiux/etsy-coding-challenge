package com.example.etsysearch.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;


public class RxUtils {
    public static Observable<CharSequence> observeEditText(EditText text) {
        Utils.checkNotNull(text, "Edit text cannot be null");

        final CharSequence defaultValue = text.getText();
        final BehaviorSubject<CharSequence> countBehaviorSubject = BehaviorSubject.create(defaultValue);

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                countBehaviorSubject.onNext(s);
            }

            @Override public void afterTextChanged(Editable s) {

            }
        });

        return countBehaviorSubject.asObservable();
    }


    public static Observable<Integer> observeEditTextCount(EditText text) {
        Utils.checkNotNull(text, "Edit text cannot be null");

        return observeEditText(text).map(new Func1<CharSequence, Integer>() {
            @Override public Integer call(CharSequence s) {
                return s.length();
            }
        });
    }

    /**
     * Note: do not use setOnEditorActionListener yourself in that case
     */
    public static Observable<KeyEvent> trackAction(EditText view, final int actionId) {
        Utils.checkNotNull(view, "view cannot be null");

        final PublishSubject<KeyEvent> publishSubject = PublishSubject.create();
        view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int eventActionId, KeyEvent event) {
                if (actionId == eventActionId) {
                    publishSubject.onNext(event);
                    return true;
                }

                return false;
            }
        });

        return publishSubject.asObservable();
    }

}
