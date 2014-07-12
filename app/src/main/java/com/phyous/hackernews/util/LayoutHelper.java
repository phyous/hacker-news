package com.phyous.hackernews.util;

import android.content.Context;

public class LayoutHelper {
    /**
     * Converts a dp to a pixel value
     * @param context of the application
     * @param dpValue which we want to ceonvert
     * @return the number of pixels corresponding to the given dp value
     */
    public static int dpToPx(Context context, int dpValue) {
        final float d = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * d);
    }
}
