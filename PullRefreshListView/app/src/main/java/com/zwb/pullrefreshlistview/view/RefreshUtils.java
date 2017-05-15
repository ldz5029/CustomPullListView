package com.zwb.pullrefreshlistview.view;

import android.widget.AbsListView;

/**
 * Created by zwb
 * Description 下拉刷新工具类
 * Date 2017/5/15.
 */

public class RefreshUtils {

    /**
     * absListView的子类是否以及下拉到最顶部
     *
     * @param absListView absListView
     * @return false
     */
    public static boolean isAbsListViewToTop(AbsListView absListView) {
        if (absListView != null) {
            int firstChildTop = 0;
            if (absListView.getChildCount() > 0) {
                // 如果AdapterView的子控件数量不为0，获取第一个子控件的top
                firstChildTop = absListView.getChildAt(0).getTop() - absListView.getPaddingTop();
            }
            //第一个child显示的下标以及距离顶部的高度都为0
            if (absListView.getFirstVisiblePosition() == 0 && firstChildTop == 0) {
                return true;
            }
        }
        return false;
    }
}
