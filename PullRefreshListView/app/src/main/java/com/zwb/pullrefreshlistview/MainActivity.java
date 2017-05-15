package com.zwb.pullrefreshlistview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zwb.pullrefreshlistview.view.ListAdapter;
import com.zwb.pullrefreshlistview.view.PullRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PullRefreshListView pullRefreshListView;
    private ListAdapter adapter;
    private List<String> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pullRefreshListView = (PullRefreshListView) findViewById(R.id.listView);
        initData();
        adapter = new ListAdapter(this, mDatas);
        pullRefreshListView.setAdapter(adapter);
        pullRefreshListView.setOnRefreshCallBack(new PullRefreshListView.OnRefreshCallBack() {
            @Override
            public void refreshing() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adData();
                        adapter.notifyData();
                        pullRefreshListView.endRefresh();
                    }
                }, 2000);
            }
        });
    }

    private static Handler handler = new Handler();

    private void initData() {
        mDatas = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            mDatas.add("pullRefreshView----->" + i);
        }
    }

    private void adData() {
        for (int i = 0; i < 3; i++) {
            mDatas.add(0, "add data----->" + i);
        }
    }

}
