package com.yzg.pulltorefresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by yzg on 2017/2/14.
 */

public class DefaultRefreshView extends RelativeLayout implements RefreshTrigger{
    private final String TAG = DefaultRefreshView.class.getSimpleName();

    /**初始状态*/
    private static final int STATE_DEFAULT = 0;
    /**下拉刷新*/
    private static final int STATE_PULL_DOWN_TO_REFRESH = 1;
    /**释放更新*/
    private static final int STATE_RELEASE_TO_UPDADE = 2;
    /**正在刷新*/
    private static final int STATE_REFRESHING = 3;
    /**完成刷新*/
    private static final int STATE_FINISHING = 4;
    /**刷新出错*/
    private static final int STATE_ERROR = 5;

    private ImageView img;
    private TextView txt;
    private ProgressBar progressBarLoading;

    /**下拉刷新主控件*/
    private RefreshLayout mRefreshLayout;

    /**刷新状态时头部的高度*/
    private int refreshingHeight = 0;

    /**当前状态*/
    private int mStatus = STATE_DEFAULT;

    /**当前刷新头部的高度*/
    private int mCurrentHeight = 0;

    /**刷新头部最大高度*/
    private int maxScrollHeight;

    /**松开手指后动画*/
    private ValueAnimator releaseAnimator;

    /**加载完成后动画*/
    private ValueAnimator finishAnimator;

    public DefaultRefreshView(Context context) {
        this(context, null);
    }

    public DefaultRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.refresh_view, this, true);

        img = (ImageView) view.findViewById(R.id.refresh_img);
        txt = (TextView) view.findViewById(R.id.refresh_txt);
        progressBarLoading = (ProgressBar) view.findViewById(R.id.progress_loading);

        refreshingHeight = DisplayUtil.dip2px(context, 50);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        maxScrollHeight = getMeasuredHeight();
    }

    @Override
    public void init(RefreshLayout refreshLayout) {
        mRefreshLayout = refreshLayout;
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if(refreshing && mStatus != STATE_REFRESHING){
            setStatus(STATE_REFRESHING);
            mRefreshLayout.onRefresh();
            move(refreshingHeight - mCurrentHeight);
        }else {
            setStatus(STATE_DEFAULT);
            move(-mCurrentHeight);
        }
    }

    @Override
    public void onTouchMove(int dy) {
        if(mStatus > STATE_RELEASE_TO_UPDADE){
            /**刷新状态只能在 0~refreshingHeight间滑动*/
            if(mCurrentHeight + dy <= refreshingHeight){
                move(dy);
            }
            return ;
        }

        if(releaseAnimator != null){
            releaseAnimator.removeAllUpdateListeners();
            releaseAnimator.cancel();
            releaseAnimator = null;
        }
        move(dy);

        if(mCurrentHeight < 1.5 * refreshingHeight){
            setStatus(STATE_PULL_DOWN_TO_REFRESH);
        }else {
            setStatus(STATE_RELEASE_TO_UPDADE);
        }

        return ;
    }

    @Override
    public void onRelease() {

        if(releaseAnimator != null){
            releaseAnimator.removeAllUpdateListeners();
            releaseAnimator.cancel();
            releaseAnimator = null;
        }

        /**刷新状态下松开手指*/
        if(mStatus > STATE_RELEASE_TO_UPDADE){
            if(mCurrentHeight < refreshingHeight / 2){
                move(-mCurrentHeight);
            }else {
                move(refreshingHeight - mCurrentHeight);
            }
            return;
        }

        if(mCurrentHeight < 1.5*refreshingHeight) {
            setStatus(STATE_DEFAULT);
            move(-mCurrentHeight);
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

    @Override
    public void onFinish(boolean success, String info) {
        if(mStatus == STATE_FINISHING || mStatus == STATE_ERROR || mStatus < STATE_REFRESHING)
            return;

        setStatus(success ? STATE_FINISHING : STATE_ERROR);
        if(!TextUtils.isEmpty(info)){
            txt.setText(info);
        }

        if(finishAnimator != null){
            finishAnimator.removeAllUpdateListeners();
            finishAnimator.cancel();
            finishAnimator = null;
        }

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
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if(finishAnimator != null)
                    finishAnimator.start();
            }
        }, 500);

    }

    private void move(int dy){
        mCurrentHeight += dy;
        if(mCurrentHeight < 0) mCurrentHeight = 0;
        if(mCurrentHeight > maxScrollHeight) mCurrentHeight = maxScrollHeight;

        mRefreshLayout.onTransformHeaderHeight(mCurrentHeight);
    }

    private void setStatus(int status){
        if(status == mStatus) return;

        switch (status){
            case STATE_DEFAULT:
                img.clearAnimation();
                img.setVisibility(VISIBLE);
                img.setImageResource(R.drawable.refresh_arrow);
                progressBarLoading.setVisibility(GONE);
                txt.setText("下拉刷新");
                break;
            case STATE_PULL_DOWN_TO_REFRESH:
                img.setVisibility(VISIBLE);
                img.setImageResource(R.drawable.refresh_arrow);
                progressBarLoading.setVisibility(GONE);
                txt.setText("下拉刷新");
                if(mStatus != STATE_DEFAULT)
                    rotateArrow(status);
                break;
            case STATE_RELEASE_TO_UPDADE:
                img.setVisibility(VISIBLE);
                img.setImageResource(R.drawable.refresh_arrow);
                progressBarLoading.setVisibility(GONE);
                txt.setText("释放更新");
                rotateArrow(status);
                break;
            case STATE_REFRESHING:
                img.clearAnimation();
                img.setVisibility(GONE);
                progressBarLoading.setVisibility(VISIBLE);
                txt.setText("加载中...");
                break;
            case STATE_FINISHING:
                img.clearAnimation();
                img.setVisibility(VISIBLE);
                img.setImageResource(R.drawable.refresh_finish);
                progressBarLoading.setVisibility(GONE);
                txt.setText("加载完成");
                break;
            case STATE_ERROR:
                img.clearAnimation();
                img.setVisibility(VISIBLE);
                img.setImageResource(R.drawable.refresh_error);
                progressBarLoading.setVisibility(GONE);
                txt.setText("加载失败");
                break;
        }
        mStatus = status;
    }

    /**旋转箭头*/
    private void rotateArrow(int status){
        img.clearAnimation();
        float pivotX = img.getWidth() / 2f;
        float pivotY = img.getHeight() / 2f;
        float fromDegrees = 0f;
        float toDegrees = 0f;

        if (status == STATE_PULL_DOWN_TO_REFRESH) {
            fromDegrees = -180f;
            toDegrees = 0f;
        } else {
            fromDegrees = 0f;
            toDegrees = -180f;
        }
        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees,
                pivotX, pivotY);
        animation.setDuration(200);
        //动画终止时停留在最后一帧
        animation.setFillAfter(true);
        //启动动画
        img.startAnimation(animation);
    }

}
