package com.app.afridge.loaders;

import com.activeandroid.query.Select;
import com.app.afridge.dom.NoteItem;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;


/**
 * Loader that gets all the notes items
 * <p/>
 * Created by drakuwa on 1/28/15.
 */
public class NotesLoader extends AsyncTaskLoader<List<NoteItem>> {

    private List<NoteItem> adapterList;

    public NotesLoader(Context context) {

        super(context);
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
    public List<NoteItem> loadInBackground() {

        adapterList = new ArrayList<>();
        adapterList = new Select()
                .from(NoteItem.class)
                        // .where("receiver_id = ?", myId)
                        // .or("sender_id = ?", myId)
                .execute();

        return adapterList;
    }

    @Override
    public void deliverResult(List<NoteItem> data) {
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
    public void onCanceled(List<NoteItem> data) {
        // try to cancel the current load and invalidate cache
        super.onCanceled(data);
        adapterList = null;
    }
}
