package com.tingz.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tingz.android.view.GroupedItemsAdapter;

/**
 * specific to the grouped items adapter - this decorator draws on the remaining space at the bottom of the recycler view
 * (so the drawable can cover the default content backgroud with default container backgroud)
 * <p/>
 * Created by steinerro on 5/02/2016.
 */
public class GroupedItemsBottomDecoration extends RecyclerView.ItemDecoration {


    final GroupedItemsAdapter itemsAdapter;

    final private Drawable drawable;

    public GroupedItemsBottomDecoration(final GroupedItemsAdapter itemsAdapter, Context context, final int dividerId) {
        this.itemsAdapter = itemsAdapter;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.drawable = context.getResources().getDrawable(dividerId, null);
        } else {
            this.drawable = context.getResources().getDrawable(dividerId);
        }
    }

    @Override
    public void onDraw(final Canvas c, final RecyclerView parent, final RecyclerView.State state) {


        View child = parent.getChildAt(parent.getChildCount() - 1);
        if (itemsAdapter.isLastItem(child)) { // this saves many many excessive computations as opposed to: child != null && parent.getChildAdapterPosition(child) == parent.getAdapter().getItemCount() - 1

            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            /*Log.v("GroupedItemsBottomDecoration", String.format("parent bounds: %s", new Rect(parent.getLeft(), parent.getTop(), parent.getRight(), parent.getBottom())));
            Log.v("GroupedItemsBottomDecoration", String.format("child bounds: %s", new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom())));
            */
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = parent.getBottom();

            if (bottom > top) {
                drawable.setBounds(left, top, right, bottom);
                drawable.draw(c);
            }
        }

    }
}
