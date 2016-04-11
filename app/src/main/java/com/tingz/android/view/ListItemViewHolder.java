package com.tingz.android.view;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tingz.android.R;

/**
 * ListItemViewHolder holds a view for a {@link android.support.v7.widget.RecyclerView.Adapter}
 * <p/>
 * this class expose a {@link LabelItemView} and a {@link IconItemView} to interact with.
 * <p/>
 * if the view held by this class does not implement {@link LabelItemView} and is
 * either one of {@link TextView}, {@link LabelItemView}
 * then an internal wrapper is created for it.
 * <p/>
 * likewise - if it does not implement {@link IconItemView} an empty wrapper is provided.
 */

public class ListItemViewHolder extends RecyclerView.ViewHolder {

    final public LabelItemView listItemView;

    public final IconItemView iconItemView;

    public ListItemViewHolder(final View itemView) {
        super(itemView);
        View view = itemView.findViewById(R.id.list_item);
        ItemViewImpl emptyImpl = new ItemViewImpl(view);
        if (view != null) { // e.g. header
            if (view instanceof LabelItemView) {
                listItemView = new IconLabelViewWrapper((LabelItemView) view);
            } else if (view instanceof TextView) {
                listItemView = new TextViewWrapper((TextView) view);
            } else {
                Log.w("GroupedItemsAdapter", String.format("ListItemViewHolder created for view with id %d. should be list_item (%d) ", view.getId(), R.id.list_item));
                listItemView = emptyImpl;
            }
            if (view instanceof IconItemView) {
                iconItemView = (IconItemView) view;
            } else if (view instanceof TextView) {
                iconItemView = new TextViewIconWrapper((TextView) view);
            } else {
                iconItemView = emptyImpl;
            }
        } else {
            listItemView = emptyImpl;
            iconItemView = emptyImpl;
        }
    }

    private class ItemViewImpl implements LabelItemView, IconItemView {

        private final View view;

        public ItemViewImpl(final View view) {
            this.view = view;
        }

        @Override
        public void setPrimaryText(final CharSequence label) {

        }

        @Override
        public void setSecondaryText(final CharSequence text) {

        }

        public void setExtraText(final CharSequence text) {

        }

        @Override
        public void setIcon(final int icon) {

        }

        public <T extends View> T asView() {
            return (T) this.view;
        }
    }

    private class IconLabelViewWrapper implements LabelItemView {

        private final LabelItemView view;

        public IconLabelViewWrapper(final LabelItemView view) {
            this.view = view;
        }

        @Override
        public void setExtraText(final CharSequence text) {
            // nothing to do here
        }

        @Override
        public void setSecondaryText(final CharSequence text) {
            // nothing to do here
        }

        @Override
        public void setPrimaryText(final CharSequence label) {
            view.setPrimaryText(label);
        }

        @Override
        public <T extends View> T asView() {
            return (T) view;
        }

    }


    /**
     * a wrapper class for time when the view is a {@link TextView}
     */
    private class TextViewWrapper implements LabelItemView {

        private final TextView view;

        public TextViewWrapper(final TextView view) {
            this.view = view;
        }

        @Override
        public void setExtraText(final CharSequence text) {

        }

        @Override
        public void setSecondaryText(final CharSequence text) {

        }

        @Override
        public void setPrimaryText(final CharSequence label) {
            view.setText(label);
        }

        @Override
        public <T extends View> T asView() {
            return (T) view;
        }

    }

    private class TextViewIconWrapper implements IconItemView {


        private final TextView textView;

        public TextViewIconWrapper(final TextView textView) {
            this.textView = textView;
        }

        @Override
        public void setIcon(final int icon) {
            if (icon > 0) {
                Drawable drawable;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = textView.getResources().getDrawable(icon, null);
                } else {
                    drawable = textView.getResources().getDrawable(icon);
                }

                textView.setCompoundDrawables(drawable, null, null, null);
            }
        }

        @Override
        public <T extends View> T asView() {
            return null;
        }
    }
}
