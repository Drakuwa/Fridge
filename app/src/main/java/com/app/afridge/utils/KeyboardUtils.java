package com.app.afridge.utils;

import android.app.Service;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


public class KeyboardUtils {

    public static void hideSoftKeyboard(View v) {

        InputMethodManager imm =
                (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void showSoftKeyboard(View v) {

        InputMethodManager imm =
                (InputMethodManager) v.getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }
}
