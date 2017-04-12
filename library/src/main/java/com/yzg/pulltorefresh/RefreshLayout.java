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
    };

    protected View mTarget;

    protected View mRefreshView;

    private int refreshRes;

    protected int offset;
    protected int mHeaderHeight;

    public RefreshLayout(Context context) {
        super(context);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));

        refreshRes = a.getResourceId(1, -1);

        a.recycle();
    }

    /**更新刷新控件头部高度*/
    public abstract void onTransformHeaderHeight(int newHeight);

    /**通知开始刷新数据*/
    public abstract void onRefresh();

    @Override
    protected void onFinishInflate() {

        ensureRefreshView();
        ensureTarget();

        super.onFinishInflate();
    }

    protected void ensureRefreshView(){
        if(refreshRes != -1){
            mRefreshView = LayoutInflater.from(getContext()).inflate(refreshRes, null);
        }

        if(!(mRefreshView instanceof RefreshTrigger)){
            mRefreshView = new DefaultRefreshView(getContext());

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mRefreshView.setLayoutParams(layoutParams);
        }

        ((RefreshTrigger)mRefreshView).init(this);

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

        mRefreshView.layout(childLeft, -(mHeaderHeight - childTop - offset), childLeft + childWidth, offset + childTop);
        mTarget.layout(childLeft, offset + childTop, childLeft + childWidth, childHeight);

        if(DEBUG){
            Log.d(TAG, String.format("Refresh view top: %d, offset: %d, height: %d", -(mHeaderHeight - childTop - offset), offset, mHeaderHeight));
        }

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChild(mRefreshView, widthMeasureSpec, heightMeasureSpec);
        measureChild(mTarget, widthMeasureSpec, heightMeasureSpec);

        mHeaderHeight = mRefreshView.getMeasuredHeight();
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
