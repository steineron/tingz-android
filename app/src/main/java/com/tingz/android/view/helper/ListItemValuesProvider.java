package com.tingz.android.view.helper;

/**
 * the interface to provide the necessities of a list item: primary, secondary and extra texts, and an icon resource id
 * <p/>
 * Created by steinerro on 25/01/2016.
 */
public interface ListItemValuesProvider<T> {

    CharSequence getPrimaryText(T model);

    CharSequence getSecondaryText(T model);

    CharSequence getExtraText(T model);

    int getIconRes(T model);
}
