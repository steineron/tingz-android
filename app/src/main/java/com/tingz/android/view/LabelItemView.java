package com.tingz.android.view;

import android.view.View;

/**
 * Created by steinerro on 1/02/2016.
 */
public interface LabelItemView {

    void setPrimaryText(CharSequence label);

    void setSecondaryText(CharSequence text);

    void setExtraText(CharSequence text);

    <T extends View> T asView();
}
