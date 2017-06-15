package com.yzg.pulltorefresh;

/**
 * Created by yzg on 2017/2/14.
 *
 * 刷新触发器
 */

public interface RefreshTrigger {

    void setStatus(@RefreshTriggerHelper.State int status);

    /**
     * 滑动回调
     * @param height 头部当前高度
     */
    void onMove(int height);

    /**正在刷新时头部的高度*/
    int getRefreshingHeight();

    /**刷新头部最大高度*/
    int getMaxScrollHeight();

    /**由下拉刷新触发释放刷新的高度*/
    int getTriggerReleaseHeight();
}
