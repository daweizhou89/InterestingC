package com.github.daweizhou89.interestingc.interfaces;

import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

/**
 * ****  *   *   ***  *  *
 * *     *   *  *     * *
 * ****  *   *  *     **
 * *     *   *  *     * *
 * *      ***    ***  *  *
 * <p/>
 * Created by zhoudawei on 16/5/25.
 */
public interface ISnackbarMaker {
    Snackbar makeSnackbar(@Nullable CharSequence text, int duration);
    boolean isFinishing();
}
