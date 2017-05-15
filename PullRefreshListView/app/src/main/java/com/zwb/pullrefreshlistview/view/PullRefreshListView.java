package com.zwb.pullrefreshlistview.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zwb.pullrefreshlistview.R;

/**
 * Created by zwb
 * Description
 * Date 2017/5/15.
 */

public class PullRefreshListView extends ListView {
    private static final String TAG = PullRefreshListView.class.getName();
    private View mHeader;//顶部刷新控件
    private ImageView refreshArrow, refreshLoad;
    private TextView tvRefreshHeaderStatus;
    private int mHeaderHeight;//顶部刷新控件的高度
    private int mMinTopPadding;//最新下拉padding
    private int mMaxTopPadding = 0;//最大下拉padding
    private int mCurTopPdding;//当前的padding

    private boolean isRemark;//添加一个标记,标记是否是在滑动到最顶部时下拉的
    private String mPullDownRefreshText = "下拉刷新";
    private String mReleaseRefreshText = "释放更新";
    private String mRefreshingText = "加载中...";

    private int mDownY;//按下是的y轴位置
    private RefreshStatus STATE = RefreshStatus.IDLE;//初始状态

    private Animation mUpAnim, mDownAnim;
    private OnRefreshCallBack onRefreshCallBack;

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
        initView(context);
        initAnimation();
    }

    /**
     * 初始化顶部刷新控件
     *
     * @param context 上下文对象
     */
    private void initView(Context context) {
        mHeader = LayoutInflater.from(context).inflate(R.layout.view_refresh_header_normal, null);
        refreshArrow = (ImageView) mHeader.findViewById(R.id.refresh_arrow);
        refreshLoad = (ImageView) mHeader.findViewById(R.id.refresh_header_load);
        tvRefreshHeaderStatus = (TextView) mHeader.findViewById(R.id.refresh_header_status);
        measureView(mHeader);
        mHeaderHeight = mHeader.getMeasuredHeight();
        mMinTopPadding = -mHeaderHeight;
        mMaxTopPadding = 30;
//        mMaxTopPadding = (int)(0.1f * mHeaderHeight);
        Log.e(TAG, "====mMinTopPadding=====" + mMinTopPadding);
        setTopPadding(mMinTopPadding);
        addHeaderView(mHeader);
    }

    /**
     * 设置顶部刷新控件的padding，来控制它的显示与隐藏
     *
     * @param topPadding
     */
    private void setTopPadding(int topPadding) {
//        Log.e(TAG,"=====topPadding====------------------------------------->"+topPadding);
        if (mHeader != null) {
            mHeader.setPadding(mHeader.getPaddingLeft(), topPadding, mHeader.getPaddingRight(), mHeader.getPaddingBottom());
            mHeader.invalidate();
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
                handleAtMove(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (STATE == RefreshStatus.RELEASE_REFRESH) {
                    STATE = RefreshStatus.REFRESHING;
                } else if (STATE == RefreshStatus.PULL_DOWN) {
                    STATE = RefreshStatus.IDLE;
                    isRemark = false;
                }
                refreshHeaderStatus();
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 处理移动
     *
     * @param ev 事件
     */
    private void handleAtMove(MotionEvent ev) {
        if (!isRemark) {//如果不是在最顶端滑动的，当滑动到最顶端是，再来计算
            if (RefreshUtils.isAbsListViewToTop(this)) {
                mDownY = (int) ev.getY();
                isRemark = true;
            }
            return;
        }
        int tempY = (int) ev.getY();
//        Log.e(TAG,"=====tempY===="+tempY);
        int fy = (int) tempY - mDownY;//设置一个下拉系数，造成下拉比较困难的感觉
//        Log.e(TAG,"====fy====="+fy);
        refreshHeaderStatus();
        //箭头滑动中的状态变化
        switch (STATE) {
            case IDLE:
                if (fy > 0) {
                    STATE = RefreshStatus.PULL_DOWN;
                    refreshHeaderStatus();
                }
                break;
            case PULL_DOWN:
                setTopPadding(fy + mMinTopPadding);
                if (fy > mHeaderHeight + 30) {//当下拉到一定高度，状态变成可释放更新状态
                    STATE = RefreshStatus.RELEASE_REFRESH;
                    refreshHeaderStatus();
                } else if (fy <= 0) {
                    STATE = RefreshStatus.IDLE;
                    isRemark = false;
                    refreshHeaderStatus();
                }
                break;
            case RELEASE_REFRESH:
                setTopPadding(fy + mMinTopPadding);
                if (fy < mHeaderHeight + 30) {//当下拉到一定高度，状态变成可释放更新状态
                    STATE = RefreshStatus.PULL_DOWN;
                    refreshHeaderStatus();
                }
                break;
        }
    }

    /**
     * 刷新顶部刷新控件的状态
     */
    private void refreshHeaderStatus() {
        switch (STATE) {
            case IDLE:
                setTopPadding(mMinTopPadding);
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
                setTopPadding(30);
                showTopLoadView();
                tvRefreshHeaderStatus.setText(mRefreshingText);
                if(onRefreshCallBack != null){
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
        mUpAnim.setDuration(150);
        mUpAnim.setFillAfter(true);

        mDownAnim = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mDownAnim.setFillAfter(true);
    }

    /**
     * 显示顶部箭头向上动画
     */
    private void showTopArrowUp() {
        refreshArrow.clearAnimation();
        refreshArrow.setVisibility(VISIBLE);
        refreshLoad.clearAnimation();
        refreshLoad.setVisibility(GONE);
        refreshArrow.startAnimation(mUpAnim);
    }

    /**
     * 显示顶部箭头向下动画
     */
    private void showTopArrowDown() {
        refreshArrow.clearAnimation();
        refreshArrow.setVisibility(VISIBLE);
        refreshLoad.clearAnimation();
        refreshLoad.setVisibility(GONE);
        refreshArrow.startAnimation(mDownAnim);
    }

    /**
     * 显示顶部刷新时的动画
     */
    private void showTopLoadView() {
        refreshArrow.clearAnimation();
        refreshArrow.setVisibility(GONE);
        refreshLoad.clearAnimation();
        refreshLoad.setVisibility(VISIBLE);
        AnimationDrawable animationDrawable = (AnimationDrawable) refreshLoad.getDrawable();
        animationDrawable.start();
    }

    /**
     * 结束下拉刷新
     */
    public void endRefresh(){
        STATE = RefreshStatus.IDLE;
        isRemark = false;
        refreshHeaderStatus();
    }

    public interface OnRefreshCallBack {
        void refreshing();
    }
}
