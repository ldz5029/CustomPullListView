package com.zwb.pullrefreshlistview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zwb.pullrefreshlistview.R;

/**
 * Created by zwb
 * Description 下拉刷新控件
 * Date 2017/5/15.
 */
public class PullRefreshListView extends ListView implements AbsListView.OnScrollListener {
    private static final String TAG = PullRefreshListView.class.getName();

    /**
     * 顶部刷控件
     */
    private View mHeader;//顶部刷新控件
    private ImageView refreshArrow, refreshHeaderLoad;
    private TextView tvRefreshHeaderStatus;
    private int mHeaderHeight;//顶部刷新控件的高度
    private int mMinTopPadding = 30;//最小下拉padding
    private int mMaxTopPadding = 0;//最大下拉padding

    /**
     * 底部加载控件
     */
    private View mFooter;//顶部刷新控件
    private ImageView refreshFooterLoad;
    private int mFooterHeight;//底部刷新控件的高度 
    private int mMaxBottomPadding = 0;//最大上拉padding 
    private int mMinBottomPadding = 30;//最小上拉padding

    private String mPullDownRefreshText = "下拉刷新";
    private String mReleaseRefreshText = "释放更新";
    private String mRefreshingText = "加载中...";
    private static final float RATIO = 1.7F;//下拉系统，系数越大，刷新越困难
    private int mDownY;//按下是的y轴位置
    private boolean isRemark;//添加一个标记,标记是否是在滑动到最顶部时下拉的，如果不在顶部下拉，等拉到顶部的时候再计算
    private RefreshStatus STATE = RefreshStatus.IDLE;//初始状态
    private Animation mUpAnim, mDownAnim;
    private OnRefreshCallBack onRefreshCallBack;

    private boolean isLoadMoreEnable = true;//是否允许上拉加载更多,默认可以 
    private boolean isLoadingMore;//是否正处于上拉加载更多的状态
    private int scrollState;//滑动状态

    private Handler handler;

    public void setLoadMoreEnable(boolean loadMoreEnable) {
        isLoadMoreEnable = loadMoreEnable;
    }

    public void setOnRefreshCallBack(OnRefreshCallBack onRefreshCallBack) {
        this.onRefreshCallBack = onRefreshCallBack;
    }

    public enum RefreshStatus {
        IDLE,  //无状态
        PULL_DOWN,  //开始下拉状态
        RELEASE_REFRESH,  //释放更新状态
        REFRESHING   //刷新中状态
    }

    public PullRefreshListView(Context context) {
        this(context, null);
    }

    public PullRefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHeaderView(context);
        initFooterView(context);
        initAnimation();
        handler = new Handler(Looper.getMainLooper());
        this.setOnScrollListener(this);
    }

    /**
     * 初始化顶部刷新控件
     *
     * @param context 上下文对象
     */
    private void initHeaderView(Context context) {
        mHeader = LayoutInflater.from(context).inflate(R.layout.view_refresh_header_normal, null);
        refreshArrow = (ImageView) mHeader.findViewById(R.id.refresh__header_arrow);
        refreshHeaderLoad = (ImageView) mHeader.findViewById(R.id.refresh_header_load);
        tvRefreshHeaderStatus = (TextView) mHeader.findViewById(R.id.refresh_header_status);
        measureView(mHeader);
        mHeaderHeight = mHeader.getMeasuredHeight();
        mMinTopPadding = -mHeaderHeight;
        setTopPadding(mMinTopPadding);
        addHeaderView(mHeader);
    }

    /**
     * 设置顶部刷新控件的padding，来控制它的显示与隐藏
     *
     * @param topPadding 距离顶部的内边距
     */
    private void setTopPadding(int topPadding) {
        if (mHeader != null && topPadding <= mMaxTopPadding && topPadding >= mMinTopPadding) {
            mHeader.setPadding(mHeader.getPaddingLeft(), topPadding, mHeader.getPaddingRight(), mHeader.getPaddingBottom());
            mHeader.invalidate();
        }
    }

    /**
     * 初始化底部刷新控件
     *
     * @param context 上下文对象
     */
    private void initFooterView(Context context) {
        mFooter = LayoutInflater.from(context).inflate(R.layout.view_refresh_footer_normal, null);
        refreshFooterLoad = (ImageView) mFooter.findViewById(R.id.refresh_footer_load);
        measureView(mFooter);
        mFooterHeight = mFooter.getMeasuredHeight();
        mMinBottomPadding = -mFooterHeight;
        setBottomPadding(mMinBottomPadding);
        addFooterView(mFooter);
    }

    /**
     * 设置底部刷新控件的padding，来控制它的显示与隐藏
     *
     * @param bottomPadding 距离底部内边距
     */
    private void setBottomPadding(int bottomPadding) {
        if (mFooter != null && bottomPadding <= mMaxBottomPadding && bottomPadding >= mMinBottomPadding) {
            mFooter.setPadding(mFooter.getPaddingLeft(), mFooter.getPaddingTop(), mFooter.getPaddingRight(), bottomPadding);
            mFooter.invalidate();
        }
    }

    /**
     * 测量子控件，告诉父控件它占多大的高度和宽度
     *
     * @param child 要测量的view
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int widthSpec = ViewGroup.getChildMeasureSpec(0, 0, MeasureSpec.UNSPECIFIED);
        int heightSpec;
        int tempHeight = lp.height;
        if (tempHeight > 0) {
            heightSpec = MeasureSpec.makeMeasureSpec(tempHeight, MeasureSpec.EXACTLY);
        } else {
            heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(widthSpec, heightSpec);
    }

    /**
     * 如果listview消费了这个事件，就不能滑动了
     *
     * @param ev 事件
     * @return true：消费这个事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (RefreshUtils.isAbsListViewToTop(this)) {
                    mDownY = (int) ev.getY();
                    isRemark = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (handleAtMove(ev)) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (STATE == RefreshStatus.RELEASE_REFRESH) {
                    STATE = RefreshStatus.REFRESHING;
                    refreshHeaderStatus();
                } else if (STATE == RefreshStatus.PULL_DOWN) {
                    STATE = RefreshStatus.IDLE;
                    refreshHeaderStatus();
                }
                isRemark = false;
                mDownY = 0;
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 处理移动
     *
     * @param ev 事件
     */
    private boolean handleAtMove(MotionEvent ev) {
        if (!isRemark) {//如果不是在最顶端滑动的，当滑动到最顶端是，再来计算
            if (RefreshUtils.isAbsListViewToTop(this)) {
                mDownY = (int) ev.getY();
                isRemark = true;
            }
            return false;
        }
        int tempY = (int) ev.getY();
        int fy = (int) ((tempY - mDownY) / RATIO);//设置一个下拉系数，造成下拉比较困难的感觉
        int padding = fy - mHeaderHeight;
        //箭头滑动中的状态变化
        switch (STATE) {
            case IDLE:
                if (fy > 0) {
                    STATE = RefreshStatus.PULL_DOWN;
                    refreshHeaderStatus();
                }
                break;
            case PULL_DOWN:
                setTopPadding(padding);
                if (padding > mMaxTopPadding) {//当下拉到一定高度，状态变成可释放更新状态
                    STATE = RefreshStatus.RELEASE_REFRESH;
                    refreshHeaderStatus();
                } else if (fy <= 0) {
                    STATE = RefreshStatus.IDLE;
                    isRemark = false;
                    refreshHeaderStatus();
                }
                break;
            case RELEASE_REFRESH:
                setTopPadding(padding);
                if (padding <= mMaxTopPadding) {//当下拉到一定高度，状态变成可释放更新状态
                    STATE = RefreshStatus.PULL_DOWN;
                    refreshHeaderStatus();
                }
                break;
        }
        if (fy > 0 && RefreshUtils.isAbsListViewToTop(this)) {
            //ACTION_DOWN 时没有消费此事件，那么子空间会处于按下状态，这里设置ACTION_CANCEL， 
            // 使子控件取消按下状态，否则子控件会执行长按事件 
            ev.setAction(MotionEvent.ACTION_CANCEL);
            super.onTouchEvent(ev);
            return true;// 当前事件被我们处理并消费--这个特别重要
        } else {
            return false;
        }
    }

    /**
     * 刷新顶部刷新控件的状态
     */
    private void refreshHeaderStatus() {
        switch (STATE) {
            case IDLE:
                hiddenRefreshHeaderView();
                break;
            case PULL_DOWN:
                showTopArrowDown();
                tvRefreshHeaderStatus.setText(mPullDownRefreshText);
                break;
            case RELEASE_REFRESH:
                showTopArrowUp();
                tvRefreshHeaderStatus.setText(mReleaseRefreshText);
                break;
            case REFRESHING:
                setTopPadding(mMaxTopPadding);
                showTopLoadView();
                tvRefreshHeaderStatus.setText(mRefreshingText);
                if (onRefreshCallBack != null) {
                    onRefreshCallBack.refreshing();
                }
                break;
        }
    }

    /**
     * 初始化箭头动画
     */
    private void initAnimation() {
        mUpAnim = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mUpAnim.setDuration(200);
        mUpAnim.setFillAfter(true);
        mDownAnim = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mDownAnim.setDuration(200);
        mDownAnim.setFillAfter(true);
    }

    /**
     * 显示顶部箭头向上动画
     */
    private void showTopArrowUp() {
        refreshArrow.clearAnimation();
        refreshArrow.setVisibility(VISIBLE);
        refreshHeaderLoad.clearAnimation();
        refreshHeaderLoad.setVisibility(GONE);
        refreshArrow.startAnimation(mUpAnim);
    }

    /**
     * 显示顶部箭头向下动画
     */
    private void showTopArrowDown() {
        refreshArrow.clearAnimation();
        refreshArrow.setVisibility(VISIBLE);
        refreshHeaderLoad.clearAnimation();
        refreshHeaderLoad.setVisibility(GONE);
        refreshArrow.startAnimation(mDownAnim);
    }

    /**
     * 显示顶部刷新时的动画
     */
    private void showTopLoadView() {
        refreshArrow.clearAnimation();
        refreshArrow.setVisibility(GONE);
        refreshHeaderLoad.clearAnimation();
        refreshHeaderLoad.setVisibility(VISIBLE);
        AnimationDrawable animationDrawable = (AnimationDrawable) refreshHeaderLoad.getDrawable();
        animationDrawable.start();
    }

    /**
     * 显示顶部刷新时的动画
     */
    private void showBottomLoadView(boolean flag) {
        if (flag) {
            refreshFooterLoad.clearAnimation();
            refreshFooterLoad.setVisibility(VISIBLE);
            AnimationDrawable animationDrawable = (AnimationDrawable) refreshFooterLoad.getDrawable();
            animationDrawable.start();
        } else {
            refreshFooterLoad.clearAnimation();
        }
    }

    /**
     * 结束下拉刷新
     */
    public void endRefresh() {
        STATE = RefreshStatus.IDLE;
        isRemark = false;
        refreshHeaderStatus();
    }

    /**
     * 结束上拉加载
     */
    public void endLoadMore() {
        //避免wifi情况下请求速度过快，加载控件一闪而过
        handler.postDelayed(mDelayHiddenLoadingMoreViewTask, 500);
    }

    private Runnable mDelayHiddenLoadingMoreViewTask = new Runnable() {
        @Override
        public void run() {
            showBottomLoadView(false);
            setBottomPadding(mMinBottomPadding);
            isLoadingMore = false;
        }
    };

    public interface OnRefreshCallBack {
        void refreshing();

        void loading();
    }

    /**
     * 隐藏下拉刷新控件--回弹动画
     */
    private void hiddenRefreshHeaderView() {
        ValueAnimator animator = ValueAnimator.ofInt(mHeader.getPaddingTop(), mMinTopPadding);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int paddingTop = (int) animation.getAnimatedValue();
                setTopPadding(paddingTop);
            }
        });
        animator.start();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
//        Log.e(TAG, "------滑动状态-------" + scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (isLoadingMore) {//如果正在进行加载更多操作，直接返回
            return;
        }
        boolean isMachScreen;//listView中的条目是否铺满屏幕，不足一屏不允许上拉加载更多 
        isMachScreen = totalItemCount > visibleItemCount;
        Log.e(TAG, "------滑动状态-------" + scrollState);
        if (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            isLoadingMore = isLoadMoreEnable && isMachScreen && RefreshUtils.isAbsListViewToBottom(this);
            Log.e(TAG, "------是否可以上拉加载-------" + isLoadingMore);
            if (isLoadingMore && onRefreshCallBack != null) {
                setBottomPadding(mMaxBottomPadding);
                onRefreshCallBack.loading();
            }
        }
    }
}