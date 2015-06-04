package com.app.afridge.utils;

import com.app.afridge.dom.FridgeItem;

import java.util.Comparator;

/**
 * Custom comparator for sorting {@link FridgeItem}
 * <p/>
 * Created by drakuwa on 3/10/15.
 */
public class FridgeItemComparator implements Comparator<FridgeItem> {

    @Override
    public int compare(FridgeItem lhs, FridgeItem rhs) {
        long lhsMillis = 0;
        long rhsMillis = 0;
        try {
            //                    if (lhs.getExpirationDate() != null)
            //                        lhsMillis = application.dateFormat.parse(lhs.getExpirationDate()).getTime();
            //                    if (rhs.getExpirationDate() != null)
            //                        rhsMillis = application.dateFormat.parse(rhs.getExpirationDate()).getTime();
            if (lhs.getExpirationDate() != 0) {
                lhsMillis = lhs.getExpirationDate();
            }
            if (rhs.getExpirationDate() != 0) {
                rhsMillis = rhs.getExpirationDate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lhsMillis < rhsMillis ? -1 : (lhsMillis == rhsMillis ? 0 : 1);
    }
}