package com.tingz.android.view.helper;

import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Created by steinerro on 2/02/2016.
 */
public interface EventTimeViewHelper {

    @NonNull
    SpannableStringBuilder getTimeTillEvent(Duration tillStart, final String displayIfPast);

    // provide the colors span used for urgent nad super urgent time texts
    @NonNull
    ForegroundColorSpan getColorUrgent();

    @NonNull
    ForegroundColorSpan getColorSuperUrgent();

    CharSequence getDate(DateTime time);

    CharSequence getTime(DateTime time);
}
