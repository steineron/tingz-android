package com.tingz.android.view;

import com.tingz.android.view.helper.EventTimeViewHelper;

import javax.annotation.Nonnull;

/**
 * Created by steinerro on 8/02/2016.
 */
public interface EventTimeView {

    @Nonnull
    EventTimeViewHelper getEventTimeViewHelper();
}
