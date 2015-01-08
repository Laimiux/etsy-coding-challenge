package com.example.etsysearch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends Activity {
    @InjectView(R.id.content) FrameLayout content;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);


        View.inflate(this, R.layout.search_container, content);
    }
}
