package com.okunev.barcodescanner;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by 777 on 1/5/2016.
 */
public class HistoryAdapter extends BaseAdapter {
    ArrayList<HistoryItem> items = new ArrayList<>();
    Context context;

    public HistoryAdapter(ArrayList<HistoryItem> items, Context context) {
        if (items != null) {
            this.items = items;
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void remove(int position){items.remove(position); notifyDataSetChanged();}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.history_item, parent, false);
        }

        Typeface face = Typeface.createFromAsset(items.get(position).getAssets(), "9607.ttf");

        TextView link = (TextView) convertView.findViewById(R.id.link);
        link.setTypeface(face);
        link.setText(items.get(position).getLink());
        TextView date = (TextView) convertView.findViewById(R.id.date);
        date.setTypeface(face);
        date.setText(items.get(position).getDate());

        return convertView;
    }
}
