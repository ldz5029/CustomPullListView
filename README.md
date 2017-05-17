# CustomPullListView
自定义下拉刷新listView
##效果展示
![这里写图片描述](https://github.com/zwb1992/CustomPullListView/blob/master/PullRefreshListView/images/pull.gif)

<!--直接在布局中申明控件-->
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.zwb.pullrefreshlistview.MainActivity">

    <com.zwb.pullrefreshlistview.view.PullRefreshListView
        android:id="@+id/listView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</RelativeLayout>

####二.回调接口

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
