package com.yzg.pulltorefresh;

/**
 * Created by yzg on 2017/2/14.
 *
 * 刷新触发器
 */

public interface RefreshTrigger {

    void init(RefreshLayout refreshLayout);

    /**直接设置刷新状态*/
    void setRefreshing(boolean refreshing);

    /**
     * 触摸滑动
     * @param dy 滑动距离 dy>0向下滑动， dy<0向上滑动
     */
    void onTouchMove(int dy);

    /**松开手指**/
    void onRelease();

    /**
     * 完成刷新
     *
     * @param success 是否刷新成功
     * @param info 附带信息
     */
    void onFinish(boolean success, String info);
}
