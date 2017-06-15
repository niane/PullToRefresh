package com.yzg.pulltorefresh;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

/**
 * Created by yzg on 2017/6/15.
 */

public class DefaultPinRefreshView extends AppCompatImageView implements RefreshTrigger {
    /**刷新状态时头部的高度*/
    private int refreshingHeight = 0;

    /**由下拉刷新触发释放刷新的高度*/
    private int triggerReleaseHeight;

    /**刷新头部最大高度*/
    private int maxScrollHeight;

    private RotateAnimation animation;

    public DefaultPinRefreshView(Context context) {
        super(context);
        setImageResource(R.drawable.refresh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        refreshingHeight = getMeasuredHeight() + 20;
        triggerReleaseHeight = getMeasuredHeight() * 3;
        maxScrollHeight = getMeasuredHeight() * 4;
    }

    @Override
    public void setStatus(@RefreshTriggerHelper.State int status) {
        if(status == RefreshTriggerHelper.STATE_REFRESHING){
            startRefreshRotation();
        }else if(status == RefreshTriggerHelper.STATE_FINISHING){
            stopRefreshRotation();
        }
    }

    @Override
    public void onMove(int height) {
        if(height <= triggerReleaseHeight){
            setRotation(1f * height / triggerReleaseHeight * 360);
        }
    }

    @Override
    public int getRefreshingHeight() {
        return refreshingHeight;
    }

    @Override
    public int getMaxScrollHeight() {
        return maxScrollHeight;
    }

    @Override
    public int getTriggerReleaseHeight() {
        return triggerReleaseHeight;
    }

    private void startRefreshRotation(){
        stopRefreshRotation();
        float pivotX = getWidth() / 2f;
        float pivotY = getHeight() / 2f;
        animation = new RotateAnimation(0, 360, pivotX, pivotY);
        animation.setDuration(600);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(-1);
        startAnimation(animation);
    }

    private void stopRefreshRotation(){
        if(animation != null){
            clearAnimation();
            animation.cancel();
            animation = null;
        }
    }
}
