package com.app.afridge.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;


public class Typefaces {

    /**
     * Another cache approach -
     * An <code>LruCache</code> for previously loaded typefaces.
     */
    // private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface get(Context c, String assetPath) {

        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(),
                            assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    Log.e(Log.TAG, "Could not get typeface '" + assetPath
                            + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }
}