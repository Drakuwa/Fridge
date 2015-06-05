package com.app.afridge.adapters;

import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.NoteItem;
import com.app.afridge.interfaces.OnNoteChangeListener;
import com.app.afridge.utils.KeyboardUtils;
import com.app.afridge.utils.Log;
import com.app.afridge.views.AutoResizeEditText;
import com.gc.materialdesign.widgets.SnackBar;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Recycler view adapter for note items
 * <p/>
 * Created by drakuwa on 2/12/15.
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private ArrayList<NoteItem> items;

    private FridgeApplication application;

    private Activity activity;

    private OnNoteChangeListener listener;

    public NotesAdapter(ArrayList<NoteItem> items, final FridgeApplication application,
            Activity activity, OnNoteChangeListener listener) {

        this.items = items;
        this.application = application;
        this.activity = activity;
        this.listener = listener;

        // sort the items by expiration date
        Collections.sort(this.items, new Comparator<NoteItem>() {

            @Override
            public int compare(NoteItem lhs, NoteItem rhs) {

                long lhsMillis = 0;
                long rhsMillis = 0;
                try {
                    if (lhs.getTimestamp() != 0) {
                        lhsMillis = lhs.getTimestamp();
                    }
                    if (rhs.getTimestamp() != 0) {
                        rhsMillis = rhs.getTimestamp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return lhsMillis > rhsMillis ? -1 : (lhsMillis == rhsMillis ? 0 : 1);
            }
        });
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final NotesAdapter.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final NoteItem item = items.get(position);
        holder.textNote.setText(item.getNote());
        holder.bindNote(item);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                item.setChecked(isChecked);
                item.save();
                holder.checkBox.setChecked(item.isChecked());
                listener.onNoteChange();

                Log.d(Log.TAG, "listener called: " + item.toString());
                //                if (isChecked) {
                //                    removeItem(position);
                //                    addItem(items.size(), item);
                //                } else {
                //                    removeItem(position);
                //                    addItem(0, item);
                //                }
            }
        });

        holder.imageDeleteNote.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // delete item it set to true, unless it gets switched by clicking on UNDO
                removeItem(position);
                final boolean[] deleteItem = {true};
                SnackBar snackBar = new SnackBar(activity,
                        String.format(application.getString(R.string.item_deleted), item.getNote()),
                        application.getString(R.string.undo), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // cancel deletion and re-insert the item
                        deleteItem[0] = false;
                        addItem(position, item);
                    }
                });
                snackBar.setOnhideListener(new SnackBar.OnHideListener() {

                    @Override
                    public void onHide() {

                        if (deleteItem[0]) {
                            // really remove the note
                            item.delete();
                            listener.onNoteChange();
                        }
                    }
                });
                snackBar.show();
            }
        });

        holder.textNote.setEnabled(true);
        holder.textNote.setFocusableInTouchMode(true);
        holder.textNote.setFocusable(true);
        holder.textNote.setEnableSizeCache(true);
        holder.textNote.setMovementMethod(null);
        // can be added after layout inflation; it doesn't have to be fixed
        // value
        holder.textNote.setMaxHeight(330);

        // dirty hack :D add on key listener to intercept the ENTER key if it is enabled
        holder.textNote.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == 66) {
                    //do your stuff here
                    if (!TextUtils.isEmpty(holder.textNote.getText())) {
                        // save the note
                        Calendar calendar = Calendar.getInstance();
                        item.setNote(holder.textNote.getText().toString().trim());
                        item.setTimestamp(calendar.getTimeInMillis() / 1000);
                        item.save();

                        KeyboardUtils.hideSoftKeyboard(holder.textNote);
                        holder.textNote.clearFocus();
                        listener.onNoteChange();
                    } else {
                        holder.textNote
                                .setError(application.getString(R.string.error_no_item_name));
                        holder.textNote.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        // add a new note on IME_ACTION_DONE
        holder.textNote.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!TextUtils.isEmpty(holder.textNote.getText())) {
                        // save the note
                        Calendar calendar = Calendar.getInstance();
                        item.setNote(holder.textNote.getText().toString().trim());
                        item.setTimestamp(calendar.getTimeInMillis() / 1000);
                        item.save();

                        KeyboardUtils.hideSoftKeyboard(holder.textNote);
                        holder.textNote.clearFocus();
                        listener.onNoteChange();
                    } else {
                        holder.textNote
                                .setError(application.getString(R.string.error_no_item_name));
                        holder.textNote.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        // we must have a text change listener to clear the error if any is set
        holder.textNote.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                holder.textNote.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // setup the initial resizing of the AutoResizeEditText
        holder.textNote.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    // @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        // holder.textNote.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        holder.textNote.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        holder.textNote.reAdjust();

                        Log.d(Log.TAG, "Text Size = " + holder.textNote.getTextSize());
                        if (holder.textNote.getTextSize() < 50f) {
                            // you can define your minSize, in this case is 50f
                            // trim all the new lines and set the text as it was
                            // before
                            holder.textNote.setText(holder.textNote.getText().toString()
                                    .replaceAll("(?m)^[ \t]*\r?\n", ""));
                        }
                    }
                });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return items.size();
    }

    public void addItem(int position, NoteItem item) {

        if (position == -1) {
            items.add(items.size(), item);
        } else {
            items.add(position, item);
        }
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {

        items.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void clearAllNotes() {

        items.clear();
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    // Our ViewHolder now implements OnClickListener and OnLongClickListener.
    public static class ViewHolder extends RecyclerView.ViewHolder {

        View root;

        NoteItem noteItem;

        // each data item is just a string in this case
        @InjectView(R.id.checkbox_note)
        android.widget.CheckBox checkBox;

        @InjectView(R.id.edit_note_name)
        AutoResizeEditText textNote;

        @InjectView(R.id.image_delete_note)
        ImageView imageDeleteNote;

        public ViewHolder(View view) {

            super(view);
            root = view;
            ButterKnife.inject(this, view);
        }

        public void bindNote(NoteItem item) {

            noteItem = item;
            checkBox.setChecked(item.isChecked());
            // setupUI(itemView, textNote);
        }

        //        public void setupUI(View view, final AutoResizeEditText aText) {
        //
        //            // if the view is not instance of AutoResizeEditText
        //            // i.e. if the user taps outside of the box
        //            if (!(view instanceof AutoResizeEditText)) {
        //
        //                view.setOnTouchListener(new View.OnTouchListener() {
        //
        //                    @Override
        //                    public boolean onTouch(View v, MotionEvent event) {
        //                        Log.d("TXTS",
        //                                "Text Size = "
        //                                        + aText.getTextSize());
        //                        if (aText.getTextSize() < 50f) {
        //                            // you can define your minSize, in this case is 50f
        //                            // trim all the new lines and set the text as it was
        //                            // before
        //                            aText.setText(aText.getText().toString().replaceAll("(?m)^[ \t]*\r?\n", ""));
        //                        }
        //
        //                        return false;
        //                    }
        //                });
        //            }
        //
        //            // If a layout container, iterate over children and seed recursion.
        //            if (view instanceof ViewGroup) {
        //                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
        //                    View innerView = ((ViewGroup) view).getChildAt(i);
        //                    setupUI(innerView, aText);
        //                }
        //            }
        //        }
    }
}
