package com.tingz.android.view.helper;


import javax.annotation.Nonnull;

/**
 * a useful wrapper for items used by recycler views or lists, especially when using up to 2 labels and an icon which is common in out app.
 * combining {@linkplain ListItemValuesProvider} to get the values once, then storing them for future use (e.g when asView is re-rendered)
 * this saves reformatting of string nad many many small memory allocations which slow the ui.
 * using different values provider parts the logic involved in creating the values, which makes it useful in different screens/flavours
 * <p/>
 * Created by steinerro on 28/01/2016.
 */

public abstract class ListItemModelWrapper<T> implements ListItemModel {

    final T model;

    int iconResource = -1;

    CharSequence primaryText;

    CharSequence secondaryText;

    CharSequence extraText;

    public ListItemModelWrapper(final T model) {
        this.model = model;
    }

    @Nonnull
    protected abstract ListItemValuesProvider<T> getItemValuesProvider();

    @Override
    public CharSequence getPrimaryText() {
        if (primaryText == null) {
            primaryText = getItemValuesProvider().getPrimaryText(model);
        }
        return primaryText;
    }

    @Override
    public CharSequence getSecondaryText() {
        if (secondaryText == null) {
            secondaryText = getItemValuesProvider().getSecondaryText(model);
        }
        return secondaryText;
    }


    @Override
    public CharSequence getExtraText() {
        if (extraText == null) {
            extraText = getItemValuesProvider().getExtraText(model);
        }
        return extraText;
    }

    @Override
    public int getIconRes() {
        if (iconResource == -1) {
            iconResource = getItemValuesProvider().getIconRes(model);
        }
        return iconResource;
    }

    // final cause you don't mess with the model
    public final T getModel() {
        return model;
    }

    @Override
    public String toString() {
        return getPrimaryText().toString();
    }
}
