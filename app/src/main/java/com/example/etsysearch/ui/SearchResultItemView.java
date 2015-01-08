package com.example.etsysearch.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.etsysearch.R;
import com.example.etsysearch.data.model.SearchResult;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by laimiux on 1/7/15.
 */
public class SearchResultItemView extends RelativeLayout {
    @InjectView(R.id.item_image) ImageView itemImage;
    @InjectView(R.id.item_title) TextView itemTitle;
    @InjectView(R.id.item_description) TextView itemDescription;

    public SearchResultItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    public void setItem(SearchResult item, Picasso picasso) {
        picasso.load(item.getMainImage().getUrl570())
                .into(itemImage);

        itemTitle.setText(item.getTitle());
        itemDescription.setText(item.getDescription());
    }
}
