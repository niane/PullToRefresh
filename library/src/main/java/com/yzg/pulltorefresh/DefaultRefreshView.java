package com.yzg.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.yzg.pulltorefresh.RefreshTriggerHelper.STATE_DEFAULT;
import static com.yzg.pulltorefresh.RefreshTriggerHelper.STATE_FINISHING;
import static com.yzg.pulltorefresh.RefreshTriggerHelper.STATE_PULL_DOWN_TO_REFRESH;
import static com.yzg.pulltorefresh.RefreshTriggerHelper.STATE_REFRESHING;
import static com.yzg.pulltorefresh.RefreshTriggerHelper.STATE_RELEASE_TO_UPDADE;


/**
 * Created by yzg on 2017/2/14.
 */

public class DefaultRefreshView extends RelativeLayout implements RefreshTrigger{
    private final String TAG = DefaultRefreshView.class.getSimpleName();

    private ImageView img;
    private TextView txt;
    private ProgressBar progressBarLoading;

    /**刷新状态时头部的高度*/
    private int refreshingHeight = 0;

    /**由下拉刷新触发释放刷新的高度*/
    private int triggerReleaseHeight;

    /**刷新头部最大高度*/
    private int maxScrollHeight;

    private int mStatus = STATE_DEFAULT;

    public DefaultRefreshView(Context context) {
        this(context, null);
    }

    public DefaultRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.refresh_view, this, true);

        img = (ImageView) view.findViewById(R.id.refresh_img);
        txt = (TextView) view.findViewById(R.id.refresh_txt);
        progressBarLoading = (ProgressBar) view.findViewById(R.id.progress_loading);

        img.setImageResource(R.drawable.refresh_arrow);

        refreshingHeight = DisplayUtil.dip2px(context, 40);
        triggerReleaseHeight = (int) (refreshingHeight * 1.5);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        maxScrollHeight = getMeasuredHeight();
    }

    public void setStatus(int status){

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
//            case STATE_ERROR:
//                img.clearAnimation();
//                img.setVisibility(VISIBLE);
//                img.setImageResource(R.drawable.refresh_error);
//                progressBarLoading.setVisibility(GONE);
//                txt.setText("加载失败");
//                break;
        }
        mStatus = status;
    }

    @Override
    public void onMove(int height) {

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

    public int getRefreshingHeight() {
        return refreshingHeight;
    }

    public int getMaxScrollHeight() {
        return maxScrollHeight;
    }

    @Override
    public int getTriggerReleaseHeight() {
        return triggerReleaseHeight;
    }
}
