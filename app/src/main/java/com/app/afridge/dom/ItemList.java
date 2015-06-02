package com.app.afridge.dom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.activeandroid.query.Select;
import com.app.afridge.dom.json.FridgeItemTypeAdapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Get the item list JSON with image path replaced
 * by Base64 encoded strings
 *
 * Created by drakuwa on 5/20/15.
 */
public class ItemList {

    private static final int MAX_IMAGE_SIZE = 300;

    public static String getItemList() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(FridgeItem.class, new FridgeItemTypeAdapter()).create();
        List<FridgeItem> fridgeItems = new Select()
                .from(FridgeItem.class).execute(); // .where("status = ?", false).execute();

        for (FridgeItem item : fridgeItems) {
            // set the image as a Base64 formatted String if it's not a type enum
            if (!TextUtils.isDigitsOnly(item.getType())) {
                // create the item bitmap, and limit its size (300KB)
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                // create the Bitmap options object
                int sampleSize = 1;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                options.inSampleSize = sampleSize; // 100%
                options.inDither = false;
                options.inScreenDensity = DisplayMetrics.DENSITY_LOW;

                Bitmap bitmap = BitmapFactory.decodeFile(item.getType(), options);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);

                byte[] byteArray = stream.toByteArray();

                double imageSizeKiloBytes = humanReadableByteCount(byteArray.length);
                while (imageSizeKiloBytes >= MAX_IMAGE_SIZE) {
                    sampleSize *= 2;

                    stream.reset();
                    stream = new ByteArrayOutputStream();

                    // re-initialize the Bitmap options object
                    options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                    options.inSampleSize = sampleSize; // 50%
                    options.inDither = false;
                    options.inScreenDensity = DisplayMetrics.DENSITY_LOW;

                    bitmap.recycle();
                    bitmap = BitmapFactory.decodeFile(item.getType(), options);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);

                    byteArray = stream.toByteArray();

                    imageSizeKiloBytes = humanReadableByteCount(byteArray.length);
                }

                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                // set the encoded image string as a type string
                item.setType(encodedImage);
            }
        }

        return gson.toJson(fridgeItems);
    }

    private static double humanReadableByteCount(long bytes) {
        int unit = 1024;
        return bytes / unit;
    }
}
