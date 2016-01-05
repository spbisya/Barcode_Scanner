package com.okunev.barcodescanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.google.zxing.Result;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private ArrayList<HistoryItem> data = new ArrayList<>();
    private DynamicListView listView;
    private ViewPager viewPager;
    private String lastScan = "spbisya";
    HistoryAdapter adapter = new HistoryAdapter(data, this);
    private SharedPreferences sPref;
    int pageNum = 0, itemsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);


        LayoutInflater inflater = LayoutInflater.from(this);
        List<View> pages = new ArrayList<>();

        View page = inflater.inflate(R.layout.activity_scan, null);
        pages.add(page);

        page = inflater.inflate(R.layout.activity_history, null);
        pages.add(page);

        MyPagerAdapter pagerAdapter = new MyPagerAdapter(pages);

        viewPager = new ViewPager(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MainActivity.this.setTitle(position == 1 ? "History" : "Barcode Reader");
                if (position == 1) {
                    invalidateOptionsMenu();
                    pageNum = 1;
                    sPref = getPreferences(MODE_PRIVATE);
                    itemsCount = sPref.getInt("itemsCount", 0);
                    data = new ArrayList<>();
                    for (int i = 0; i < itemsCount; i++) {

                        data.add(new HistoryItem(sPref.getString("link" + i, ""), sPref.getString("data" + i, ""), getAssets()));
                    }
                    listView = (DynamicListView) findViewById(R.id.history);
                    adapter = new HistoryAdapter(data, MainActivity.this);

                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final int pos = position;
                            new AlertDialog.Builder(MainActivity.this)
                                    .setItems(R.array.list_options, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            switch (whichButton) {
                                                case 0:
                                                    adapter.remove(pos);
                                                    listView.setAdapter(adapter);
                                                    break;
                                                case 1:
                                                    String url = data.get(pos).getLink();
                                                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                                                        url = "http://" + url;
                                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                    startActivity(browserIntent);
                                                    break;
                                            }
                                        }
                                    }).show();

                        }
                    });
                } else if (position == 0) {
                    invalidateOptionsMenu();
                    pageNum = 0;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setContentView(viewPager);

    }

    public void onclik(View v) {
        setContentView(mScannerView);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        if (!lastScan.equals(result.getText())) {
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            data.add(0, new HistoryItem(result.getText(), checkDate("" + (today.yearDay + 1)) + "/" +
                    checkDate("" + (today.month + 1)) + "/" + today.year + "   "
                    + checkDate("" + today.hour) + ":" + checkDate("" + today.minute) + ":" +
                    checkDate("" + today.second), getAssets()));
            lastScan = result.getText();
        }
        mScannerView.resumeCameraPreview(this);
    }

    public String checkDate(String date) {
        String newDate = date;
        if (date.length() == 1) {
            newDate = "0" + date;
        }
        return newDate;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_list, menu);
        if (pageNum == 0)
            menu.findItem(R.id.clear).setVisible(false);
        else if (pageNum == 1)
            menu.findItem(R.id.clear).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            android.support.v7.app.AlertDialog.Builder alertDialog1 =
                    new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
            alertDialog1.setTitle("Clear history?");
            alertDialog1.setIcon(R.drawable.ic_clear_all_black_48dp);
            alertDialog1.setPositiveButton("Clear",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            data = new ArrayList<>();
                            adapter = new HistoryAdapter(data, MainActivity.this);
                            listView = (DynamicListView) findViewById(R.id.history);
                            listView.setAdapter(adapter);
                            sPref = getPreferences(MODE_PRIVATE);
                            SharedPreferences.Editor ed = sPref.edit();
                            ed.clear().commit();
                        }
                    });

            alertDialog1.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog1.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (data != null) {
            sPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putInt("itemsCount", data.size());
            for (int i = 0; i < data.size(); i++) {
                ed.putString("link" + i, data.get(i).getLink());
                ed.putString("data" + i, data.get(i).getDate());
            }
            ed.commit();
        }
        mScannerView.stopCamera();
        setContentView(viewPager);
        viewPager.setCurrentItem(1);
        adapter = new HistoryAdapter(data, this);
        listView = (DynamicListView) findViewById(R.id.history);
        listView.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (data != null) {
            sPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putInt("itemsCount", data.size());
            for (int i = 0; i < data.size(); i++) {
                ed.putString("link" + i, data.get(i).getLink());
                ed.putString("data" + i, data.get(i).getDate());
            }
            ed.commit();
        }
    }

}
