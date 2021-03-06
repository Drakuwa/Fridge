package com.app.afridge.views;

import com.app.afridge.R;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.Typefaces;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;


public class AdvancedButtonView extends Button {

    public AdvancedButtonView(Context context) {

        super(context);
    }

    public AdvancedButtonView(Context context, AttributeSet attrs) {

        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public AdvancedButtonView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {

        TypedArray a = ctx.obtainStyledAttributes(attrs,
                R.styleable.AdvancedTextView);
        String customFont = a
                .getString(R.styleable.AdvancedTextView_customFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset) {

        Typeface tf;
        try {
            tf = Typefaces.get(ctx, asset); // Typeface.createFromAsset(ctx.getAssets(), asset);
        } catch (Exception e) {
            Log.e(Log.TAG, "Could not get typeface: " + e.getMessage());
            return false;
        }

        setTypeface(tf);
        return true;
    }

}