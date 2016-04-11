package com.tingz.android.view;

import android.view.View;

/**
 * Created by steinerro on 1/02/2016.
 */
public interface IconItemView {

    void setIcon(int icon);

    <T extends View> T asView();
}
