package com.example.nan.ssprocess.util;

/**
 * Created by Hu Tong on 9/8/2016.
 */
import android.content.Context;
import android.view.WindowManager;

public final class ScreenUtil {

    public static int getScreenWidth(Context context) {
        WindowManager windowMgr = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return windowMgr.getDefaultDisplay().getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowMgr = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return windowMgr.getDefaultDisplay().getHeight();
    }

    private ScreenUtil() {
    }
}