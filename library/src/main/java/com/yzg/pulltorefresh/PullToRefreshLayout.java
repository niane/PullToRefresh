package com.yzg.pulltorefresh;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class PullToRefreshLayout extends RefreshLayout implements NestedScrollingParent, NestedScrollingChild {
    private static final String TAG = PullToRefreshLayout.class.getSimpleName();

    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .5f;

    private float mLastTouchY = 0;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    private int mTouchSlop;

    private OnRefreshListener mListener;

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private boolean mNestedScrollInProgress;

    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    /**
     * 完成刷新
     */
    public void finish() {
        triggerHelper.finish();
    }

    /**
     * 开始刷新
     */
    public void start() {
        triggerHelper.startRefresh();
    }

    public boolean isRefreshing() {
        return triggerHelper.isRefreshing();
    }

    @Override
    protected void onTransformHeaderHeight(int newHeight) {
        mRefreshView.offsetTopAndBottom(newHeight - mCurrentHeight);
        if(!pin) {
            mTarget.offsetTopAndBottom(newHeight - mCurrentHeight);
        }
        mCurrentHeight = newHeight;
        Log.d("Test", "New height: " + mCurrentHeight);
    }

    @Override
    protected void onRefresh() {
        if (mListener != null) {
            mListener.onRefresh();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);

        if (!isEnabled() || mNestedScrollInProgress || triggerHelper.isRefreshing()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(pointerIndex);
                mLastTouchY = ev.getY(pointerIndex);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mActivePointerId = ev.getPointerId(pointerIndex);
                mLastTouchY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER ||
                        mActivePointerId != ev.getPointerId(pointerIndex)) {
                    return false;
                }

                float dy = ev.getY(pointerIndex) - mLastTouchY;

                if (!mIsBeingDragged && Math.abs(dy) > mTouchSlop) {
                    mLastTouchY = ev.getY(pointerIndex);
                    mIsBeingDragged = dy > 0 ? !canTargetScrollDown() : mCurrentHeight > 0;
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = MotionEventCompat.getActionIndex(ev);

        if (!isEnabled() || mNestedScrollInProgress || triggerHelper.isRefreshing()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(pointerIndex);
                mLastTouchY = ev.getY(pointerIndex);
                mIsBeingDragged = false;
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                mActivePointerId = ev.getPointerId(pointerIndex);
                mLastTouchY = ev.getY(pointerIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER ||
                        mActivePointerId != ev.getPointerId(pointerIndex)) {
                    return false;
                }

                if (mIsBeingDragged) {
                    final float y = ev.getY(pointerIndex);
                    final float rate = y - mLastTouchY > 0 ? DRAG_RATE : 1;

                    final int dy = (int) ((y - mLastTouchY) * rate);
                    mLastTouchY = y;
                    triggerHelper.touchMove(dy);
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    final float y = ev.getY(pointerIndex);
                    final int dy = (int) ((y - mLastTouchY) * DRAG_RATE);
                    mLastTouchY = y;
                    triggerHelper.touchMove(dy);
                    triggerHelper.release();
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }


    /****       NestedScrollingParent           ***/
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mNestedScrollInProgress = true;
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        //向上
        if (dy > 0 && mCurrentHeight > 0 && !triggerHelper.isRefreshing()) {
            consumed[1] = Math.min(mCurrentHeight, dy);
            triggerHelper.touchMove(-consumed[1]);
        }

        final int[] parentConsumed = new int[2];
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }

    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        //向下
        if (dyUnconsumed < 0 && !canTargetScrollDown() && !triggerHelper.isRefreshing()) {
            triggerHelper.touchMove((int) (-dyUnconsumed * DRAG_RATE));
        } else {
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null);
        }
    }

    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;

        if (mCurrentHeight > 0 && !triggerHelper.isRefreshing()) {
            triggerHelper.release();
        }

        stopNestedScroll();
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }


    /***       NestedScrollingChild      ***/
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                        int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }


    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }


    public interface OnRefreshListener {
        /**
         * Called when a swipe gesture triggers a start.
         */
        void onRefresh();
    }

}
