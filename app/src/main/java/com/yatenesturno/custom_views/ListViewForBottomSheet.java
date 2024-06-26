package com.yatenesturno.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

public class ListViewForBottomSheet extends ListView {

    public ListViewForBottomSheet(Context p_context, AttributeSet p_attrs) {
        super(p_context, p_attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent p_event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent p_event) {
        if (canScrollVertically(this)) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(p_event);
    }

    public boolean canScrollVertically(AbsListView view) {

        boolean canScroll = false;

        if (view != null && view.getChildCount() > 0) {

            boolean isOnTop = view.getFirstVisiblePosition() != 0 || view.getChildAt(0).getTop() != 0;
            boolean isAllItemsVisible = isOnTop && getLastVisiblePosition() == view.getChildCount();

            if (isOnTop || isAllItemsVisible) {
                canScroll = true;
            }
        }

        return canScroll;
    }

}
