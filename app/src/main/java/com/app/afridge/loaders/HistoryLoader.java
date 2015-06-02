package com.app.afridge.loaders;

import com.activeandroid.query.Select;
import com.app.afridge.dom.HistoryItem;
import com.app.afridge.utils.Log;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;


/**
 * Loader that gets all the fridge items
 * <p/>
 * Created by drakuwa on 1/28/15.
 */
public class HistoryLoader extends AsyncTaskLoader<List<HistoryItem>> {

    private List<HistoryItem> adapterList;

    private long filterTimestamp;

    public HistoryLoader(Context context, long filterTimestamp) {

        super(context);
        this.filterTimestamp = filterTimestamp;
    }

    @Override
    protected void onStartLoading() {
        // use cached results
        if (adapterList != null) {
            deliverResult(adapterList);
        }
        // reload data
        else {
            forceLoad();
        }

    }

    @Override
    protected void onStopLoading() {
        // cancel the ongoing load if any
        cancelLoad();
    }

    @Override
    public List<HistoryItem> loadInBackground() {

        adapterList = new ArrayList<>();
        if (filterTimestamp != 0) {
            adapterList = new Select()
                    .from(HistoryItem.class)
                    .where("timestamp > ?", filterTimestamp)
                            // .and("timestamp > ?", filterType)
                    .execute();
        } else {
            adapterList = new Select()
                    .from(HistoryItem.class)
                            // .or("sender_id = ?", myId)
                    .execute();
        }
        Log.d(Log.TAG,
                "filterTimestamp: " + filterTimestamp + "; adapterList.size(): " + adapterList
                        .size());
        return adapterList;
    }

    @Override
    public void deliverResult(List<HistoryItem> data) {
        // cache data
        adapterList = data;
        super.deliverResult(data);
    }

    @Override
    protected void onReset() {

        super.onReset();
        // stop loader if running
        onStopLoading();
        // invalidate cache
        adapterList = null;
    }

    @Override
    public void onCanceled(List<HistoryItem> data) {
        // try to cancel the current load and invalidate cache
        super.onCanceled(data);
        adapterList = null;
    }
}
