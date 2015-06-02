package com.app.afridge.views;

import com.app.afridge.R;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.Typefaces;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class AdvancedTextView extends TextView {

    public AdvancedTextView(Context context) {

        super(context);
    }

    public AdvancedTextView(Context context, AttributeSet attrs) {

        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public AdvancedTextView(Context context, AttributeSet attrs, int defStyle) {

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