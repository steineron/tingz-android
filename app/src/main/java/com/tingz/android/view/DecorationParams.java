package com.tingz.android.view;

/**
 * all list items are expected to have decoration info
 */

public class DecorationParams {

    public Object type;

    public int left;

    public int right;

    public DecorationParams(final Object type) {
        this.type = type;
    }
}
