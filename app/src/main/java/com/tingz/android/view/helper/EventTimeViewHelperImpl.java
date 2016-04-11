package com.tingz.android.view.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

import com.tingz.android.R;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;


/**
 * Created by steinerro on 2/02/2016.
 */
public class EventTimeViewHelperImpl implements EventTimeViewHelper {

    protected final PeriodFormatter periodFormatter;

    protected final PeriodFormatter closeTimePeriodFormatter;

    final Context context;

    protected ForegroundColorSpan colorSuperUrgent;

    protected ForegroundColorSpan colorUrgent;

    private final DateTimeFormatter timeFormatter;

    private final DateTimeFormatter dateFormatter;

    public EventTimeViewHelperImpl(final Context context) {
        this.context = context;

        colorSuperUrgent = new ForegroundColorSpan(context.getResources().getColor(R.color.text_urgent));
        closeTimePeriodFormatter = new PeriodFormatterBuilder()
                .appendMinutes()
                .appendSuffix("m ", "m ")
                .printZeroRarelyFirst()
                .appendSeconds()
                .appendSuffix("s", "s")
                .toFormatter();

        colorUrgent = new ForegroundColorSpan(context.getResources().getColor(R.color.text_super_urgent));
        periodFormatter = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix("d ", "d ")
                .printZeroRarelyFirst()
                .appendHours()
                .appendSuffix("h ", "h ")
                .printZeroRarelyFirst()
                .appendMinutes()
                .appendSuffix("m", "m")
                .toFormatter();
        dateFormatter = new DateTimeFormatterBuilder()
                .appendMonthOfYearText()
                .appendLiteral(' ')
                .appendDayOfMonth(1)
                .appendLiteral(", ")
                .appendYear(4, 4)
                .toFormatter();
        timeFormatter = new DateTimeFormatterBuilder()
                .appendHourOfDay(2)
                .appendLiteral(':')
                .appendMinuteOfHour(2)
                .toFormatter();
    }

    @Override
    @NonNull
    public ForegroundColorSpan getColorSuperUrgent() {
        return colorSuperUrgent;
    }

    @Override
    public CharSequence getDate(final DateTime time) {
        return dateFormatter.print(time);
    }

    @Override
    public CharSequence getTime(final DateTime time) {
        return timeFormatter.print(time);
    }

    @Override
    @NonNull
    public ForegroundColorSpan getColorUrgent() {
        return colorUrgent;
    }

    @Override
    @NonNull
    public SpannableStringBuilder getTimeTillEvent(final Duration tillStart, final String displayIfPast) {
        long minutes = tillStart.getStandardMinutes();
        long seconds = tillStart.getStandardSeconds();
        PeriodFormatter formatter = Math.abs(minutes) < 3 && minutes >= 0 ?
                closeTimePeriodFormatter :
                periodFormatter;

        String text = seconds > 0 || displayIfPast == null ?
                formatter.print(tillStart.toPeriod()) :
                displayIfPast;

        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        if (minutes <= 30 && text != null && text.length() > 1) {
            ForegroundColorSpan span = minutes < 15 ?
                    colorSuperUrgent :
                    colorUrgent;

            builder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        if (seconds > 0) {
            // scale down the d,h,m and s in the string
            int size = context.getResources().getDimensionPixelSize(R.dimen.time_marks_text_size);

            int d = text.indexOf("d");
            if (d >= 0) {
                builder.setSpan(new AbsoluteSizeSpan(size), d, d + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            int h = text.indexOf("h");
            if (h >= 0) {
                builder.setSpan(new AbsoluteSizeSpan(size), h, h + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            int m = text.indexOf("m");
            if (m >= 0) {
                builder.setSpan(new AbsoluteSizeSpan(size), m, m + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            int s = text.indexOf("s");
            if (s >= 0) {
                builder.setSpan(new AbsoluteSizeSpan(size), s, s + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return builder;
    }
}
