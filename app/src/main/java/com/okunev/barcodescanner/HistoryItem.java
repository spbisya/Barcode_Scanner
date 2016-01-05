package com.okunev.barcodescanner;

import android.content.res.AssetManager;

/**
 * Created by 777 on 1/5/2016.
 */
public class HistoryItem {
    public String link, date;
    public AssetManager assets;

    public HistoryItem(String link, String date, AssetManager assets) {
        this.link = link;
        this.date = date;
        this.assets = assets;
    }

    public AssetManager getAssets() {
        return assets;
    }

    public String getLink() {
        return link;
    }

    public String getDate() {
        return date;
    }
}
