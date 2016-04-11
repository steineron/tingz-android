package com.tingz.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tingz.android.view.GroupedItemsAdapter;

/**
 * Created by steinerro on 5/02/2016.
 */
public class GroupedItemsDividerDecoration extends RecyclerView.ItemDecoration {

    final GroupedItemsAdapter itemsAdapter;

    final Drawable divider;

    public GroupedItemsDividerDecoration(final GroupedItemsAdapter itemsAdapter, Context context, final int dividerId) {
        this.itemsAdapter = itemsAdapter;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.divider = context.getResources().getDrawable(dividerId, null);
        } else {
            this.divider = context.getResources().getDrawable(dividerId);
        }
    }

    @Override
    public void onDraw(final Canvas c, final RecyclerView parent, final RecyclerView.State state) {
        int children = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < children; i++) {
            View child = parent.getChildAt(i);
            if (itemsAdapter.isItemDecorated(child)) { // only items not decorated by the adapter. i.e header, footer and first/last row of a group
                continue;
            }

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int bottom = itemsAdapter.isItemDecorated(view) ?
                0 :
                divider.getIntrinsicHeight();
        outRect.set(0, 0, 0, bottom);
    }
}
