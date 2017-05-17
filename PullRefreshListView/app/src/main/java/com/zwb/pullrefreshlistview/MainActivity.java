package com.zwb.pullrefreshlistview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.zwb.pullrefreshlistview.view.ListAdapter;
import com.zwb.pullrefreshlistview.view.PullRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PullRefreshListView pullRefreshListView;
    private ListAdapter adapter;
    private List<String> mDatas = new ArrayList<>();
    ;

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
                Toast.makeText(MainActivity.this, "正在下拉刷新中", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                        adapter.notifyDataSetChanged();
                        pullRefreshListView.endRefresh();
                    }
                }, 2000);
            }

            @Override
            public void loading() {
                Toast.makeText(MainActivity.this, "正在加载更多中", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adData();
                        adapter.notifyDataSetChanged();
                        pullRefreshListView.endLoadMore();
                    }
                }, 1000);
            }
        });

        pullRefreshListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "长按事件", Toast.LENGTH_SHORT).show();
                mDatas.remove(position - 1);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private static Handler handler = new Handler();

    private void initData() {
        mDatas.clear();
        for (int i = 0; i < 15; i++) {
            mDatas.add("pullRefreshView----->" + i);
        }
    }

    private void adData() {
        for (int i = 0; i < 2; i++) {
            mDatas.add("add data----->" + i);
        }
    }

}
