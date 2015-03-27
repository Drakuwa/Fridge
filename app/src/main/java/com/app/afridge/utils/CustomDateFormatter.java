package com.app.afridge.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class CustomDateFormatter {

  private static final CustomDateFormatter instance = new CustomDateFormatter();
  private SimpleDateFormat sdf;

  private CustomDateFormatter() {
    // sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH); "2014-11-20T14:35:37Z"
    sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.ENGLISH); // "yyyy-MM-ddThh:mm:ssZ"
  }

  public SimpleDateFormat getDateFormat() {

    return sdf;
  }

  public static CustomDateFormatter getInstance() {

    return instance;
  }
}
