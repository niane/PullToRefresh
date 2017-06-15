package com.yzg.pulltorefresh;

import android.animation.ValueAnimator;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by yzg on 2017/6/13.
 */

public class RefreshTriggerHelper {
    /**初始状态*/
    public static final int STATE_DEFAULT = 0;
    /**下拉刷新*/
    public static final int STATE_PULL_DOWN_TO_REFRESH = 1;
    /**释放更新*/
    public static final int STATE_RELEASE_TO_UPDADE = 2;
    /**正在刷新*/
    public static final int STATE_REFRESHING = 3;
    /**完成刷新*/
    public static final int STATE_FINISHING = 4;
    /**刷新出错*/
//    private static final int STATE_ERROR = 5;

    @IntDef({STATE_DEFAULT, STATE_PULL_DOWN_TO_REFRESH, STATE_RELEASE_TO_UPDADE, STATE_REFRESHING, STATE_FINISHING})
    @Retention(RetentionPolicy.SOURCE)
    @interface State{}

    /**下拉刷新主控件*/
    private RefreshLayout mRefreshLayout;

    private RefreshTrigger mRefreshTrigger;

    /**刷新状态时头部的高度*/
    private int refreshingHeight;

    /**由下拉刷新触发释放刷新的高度*/
    private int triggerReleaseHeight;

    /**刷新头部最大高度*/
    private int maxScrollHeight;

    /**当前刷新头部的高度*/
    private int mCurrentHeight = 0;

    /**当前状态*/
    private @State int mStatus = STATE_DEFAULT;

    /**松开手指后动画*/
    private ValueAnimator releaseAnimator;

    /**加载完成后动画*/
    private ValueAnimator finishAnimator;

    public RefreshTriggerHelper(RefreshLayout refreshLayout, RefreshTrigger refreshTrigger) {
        this.mRefreshLayout = refreshLayout;
        this.mRefreshTrigger = refreshTrigger;
    }

    public void startRefresh() {
        if(mStatus < STATE_REFRESHING){
            setStatus(STATE_REFRESHING);
            mRefreshLayout.onRefresh();
            move(refreshingHeight - mCurrentHeight);
        }
    }

    public boolean touchMove(int dy) {
        if(mStatus > STATE_RELEASE_TO_UPDADE){
            /*********正在刷新*********/
            return false;
        }

        if(releaseAnimator != null){
            releaseAnimator.removeAllUpdateListeners();
            releaseAnimator.cancel();
            releaseAnimator = null;
        }
        move(dy);

        if(mCurrentHeight < triggerReleaseHeight){
            setStatus(STATE_PULL_DOWN_TO_REFRESH);
        }else {
            setStatus(STATE_RELEASE_TO_UPDADE);
        }

        return true;
    }

    public void release() {

        if(releaseAnimator != null){
            releaseAnimator.removeAllUpdateListeners();
            releaseAnimator.cancel();
            releaseAnimator = null;
        }

        stopFinishAnimator();

        if(mCurrentHeight < triggerReleaseHeight) {
            startFinishAnimator();
            return;
        }

        releaseAnimator = ValueAnimator.ofInt(mCurrentHeight, refreshingHeight);
        releaseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(animation == null) return;
                int h = Math.max(refreshingHeight, (int) animation.getAnimatedValue());

                if(h == refreshingHeight){
                    setStatus(STATE_REFRESHING);
                    mRefreshLayout.onRefresh();
                }
                move(h - mCurrentHeight);
            }
        });
        releaseAnimator.setDuration(500);
        releaseAnimator.start();
    }

    public void finish() {
        if(mStatus == STATE_FINISHING)
            return;

        stopFinishAnimator();
        setStatus(STATE_FINISHING);
        startFinishAnimator();
    }

    private void stopFinishAnimator(){
        if(finishAnimator != null){
            finishAnimator.removeAllUpdateListeners();
            finishAnimator.cancel();
            finishAnimator = null;
        }
    }

    private void startFinishAnimator(){
        finishAnimator = ValueAnimator.ofInt(mCurrentHeight, 0);
        finishAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(animation == null) return;
                int h = Math.max(0, (int) animation.getAnimatedValue());

                if(h == 0){
                    setStatus(STATE_DEFAULT);
                }
                move(h - mCurrentHeight);
            }
        });
        finishAnimator.setDuration(300);
        finishAnimator.start();
    }

    private void move(int dy){
        mCurrentHeight += dy;
        if(mCurrentHeight < 0) mCurrentHeight = 0;
        if(mCurrentHeight > maxScrollHeight) mCurrentHeight = maxScrollHeight;

        mRefreshLayout.onTransformHeaderHeight(mCurrentHeight);
        mRefreshTrigger.onMove(mCurrentHeight);
    }

    private void setStatus(@State int status){
        if(mStatus == status) return;
        mStatus = status;
        mRefreshTrigger.setStatus(status);
    }

    public boolean isRefreshing(){
        return mStatus > STATE_RELEASE_TO_UPDADE;
    }

    public void setRefreshingHeight(int refreshingHeight) {
        this.refreshingHeight = refreshingHeight;
    }

    public void setTriggerReleaseHeight(int triggerReleaseHeight) {
        this.triggerReleaseHeight = triggerReleaseHeight;
    }

    public void setMaxScrollHeight(int maxScrollHeight) {
        this.maxScrollHeight = maxScrollHeight;
    }
}
