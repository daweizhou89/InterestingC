package com.github.daweizhou89.interestingc.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.github.daweizhou89.interestingc.constant.Constant;
import com.github.daweizhou89.interestingc.interfaces.ISnackbarMaker;


/**
 * ****  *   *   ***  *  *
 * *     *   *  *     * *
 * ****  *   *  *     **
 * *     *   *  *     * *
 * *      ***    ***  *  *
 * <p/>
 * Created by zhoudawei on 16/5/25.
 */
public class ToastUtil {

    /**
     * Make a Toast/Snackbar to display a message
     *
     * @param context
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link Toast.LENGTH_SHORT} or {@link
     *                 Toast.LENGTH_LONG}
     */
    public static ToastWrapper makeText(@NonNull Context context, @NonNull CharSequence text, int duration) {
        if (context == null || text == null) {
            // 避免代码上低级错误导致线上包crash, 正式环境不抛出异常
            if (Constant.TEST) {
                throw new RuntimeException("ToastUtil.makeText参数context或text不能为空");
            } else {
                return new ToastWrapper();
            }
        }
        Snackbar snackbar = null;
        if (context instanceof ISnackbarMaker) {
            ISnackbarMaker snackbarMaker = (ISnackbarMaker) context;
            if (!snackbarMaker.isFinishing()) {
                final int durationSnackbar = duration == Toast.LENGTH_LONG ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT;
                try {
                    snackbar = (snackbarMaker).makeSnackbar(text, durationSnackbar);
                } catch (Throwable e) {
                    // 如果Activity没有配AppCompat主题会有异常
                    // TODO nothing
                }
            }
        }
        if (snackbar != null) {
            return new ToastWrapper(snackbar);
        } else {
            return new ToastWrapper(Toast.makeText(context, text, duration));
        }
    }

    /**
     * Make a Toast/Snackbar to display a message
     *
     * @param context
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link Toast.LENGTH_SHORT} or {@link
     *                 Toast.LENGTH_LONG}
     */
    public static ToastWrapper makeText(@NonNull Context context, int text, int duration) {
        return makeText(context, context.getString(text), duration);
    }

    public static class ToastWrapper {

        private Snackbar mSnackbar;

        private Toast mToast;

        public ToastWrapper() {}

        public ToastWrapper(Snackbar snackbar) {
            mSnackbar = snackbar;
        }

        public ToastWrapper(Toast toast) {
            mToast = toast;
        }

        public void show() {
            if (mSnackbar != null) {
                mSnackbar.show();
            }
            if (mToast != null) {
                mToast.show();
            }
        }
    }

}
