package com.yzg.pulltorefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * Created by yzg on 2017/2/21.
 */

abstract class RefreshLayout extends ViewGroup{
    private final String TAG = RefreshLayout.class.getSimpleName();
    private final boolean DEBUG = true;

    private static final int[] LAYOUT_ATTRS = new int[] {
            android.R.attr.enabled,
            R.attr.refresh_layout,
            R.attr.pin
    };

    protected View mTarget;

    protected View mRefreshView;

    protected RefreshTrigger refreshTrigger;

    protected RefreshTriggerHelper triggerHelper;

    private int refreshRes;

    /**mRefreshView当前的位置**/
    protected int mCurrentHeight;

    /**Target是否固定**/
    protected boolean pin = false;

    public RefreshLayout(Context context) {
        super(context);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        refreshRes = a.getResourceId(1, -1);
        pin = a.getBoolean(2, false);

        a.recycle();
    }

    /**更新刷新控件头部高度*/
    protected abstract void onTransformHeaderHeight(int newHeight);

    /**通知开始刷新数据*/
    protected abstract void onRefresh();

    @Override
    protected void onFinishInflate() {

        ensureRefreshView();
        ensureTarget();
        triggerHelper = new RefreshTriggerHelper(this, refreshTrigger);

        super.onFinishInflate();
    }

    protected void ensureRefreshView(){
        if(refreshRes != -1){
            mRefreshView = LayoutInflater.from(getContext()).inflate(refreshRes, null);
        }

        if(!(mRefreshView instanceof RefreshTrigger)){

            if(!pin) {
                mRefreshView = new DefaultRefreshView(getContext());

                LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mRefreshView.setLayoutParams(layoutParams);
            }else {
                mRefreshView = new DefaultPinRefreshView(getContext());

                LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mRefreshView.setLayoutParams(layoutParams);
            }
        }

        refreshTrigger = (RefreshTrigger) mRefreshView;

        addView(mRefreshView);
    }

    protected void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!(child instanceof RefreshTrigger)) {
                    mTarget = child;
                    break;
                }
            }
        }

        if(mTarget == null){
            throw new NullPointerException("Target cannot be null");
        }
    }

    public boolean canTargetScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }

        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();

        final int refreshChildWidth = mRefreshView.getMeasuredWidth();
        final int refreshChildHeight = mRefreshView.getMeasuredHeight();
        final int refreshChildLeft = (width - refreshChildWidth)/2;

        mRefreshView.layout(refreshChildLeft, childTop + mCurrentHeight - refreshChildHeight, refreshChildLeft + refreshChildWidth, mCurrentHeight + childTop);

        if(pin){
            mTarget.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }else {
            mTarget.layout(childLeft, mCurrentHeight + childTop, childLeft + childWidth, mCurrentHeight + childTop + mTarget.getMeasuredHeight());
        }
        if(DEBUG){
            Log.d(TAG, String.format("Refresh view top: %d, currentHeight: %d, height: %d", -(refreshChildHeight - childTop - mCurrentHeight), mCurrentHeight, refreshChildHeight));
        }

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChild(mRefreshView, widthMeasureSpec, heightMeasureSpec);
        measureChild(mTarget, widthMeasureSpec, heightMeasureSpec);

        triggerHelper.setMaxScrollHeight(refreshTrigger.getMaxScrollHeight());
        triggerHelper.setRefreshingHeight(refreshTrigger.getRefreshingHeight());
        triggerHelper.setTriggerReleaseHeight(refreshTrigger.getTriggerReleaseHeight());
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        if ((android.os.Build.VERSION.SDK_INT < 21 && mTarget instanceof AbsListView)
                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }
}
