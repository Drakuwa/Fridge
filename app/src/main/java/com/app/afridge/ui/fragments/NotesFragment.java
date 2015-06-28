package com.app.afridge.ui.fragments;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.adapters.NotesAdapter;
import com.app.afridge.dom.NoteItem;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.interfaces.OnNoteChangeListener;
import com.app.afridge.loaders.NotesLoader;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.KeyboardUtils;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.SharedPrefStore;
import com.app.afridge.utils.TimeSpans;
import com.app.afridge.views.AdvancedAutoCompleteTextView;
import com.app.afridge.views.AdvancedTextView;
import com.gc.materialdesign.widgets.SnackBar;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Notes items - A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Created by drakuwa on 1/28/15.
 */
public class NotesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<NoteItem>>,
        OnNoteChangeListener {

    private static final String KEY_CONTENT = "NotesFragment:Content";

    // Singleton
    private static volatile NotesFragment instance = null;

    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;

    @InjectView(android.R.id.empty)
    LinearLayout emptyText;

    @InjectView(R.id.edit_list_name)
    AdvancedAutoCompleteTextView textListName;

    @InjectView(R.id.edit_list_item)
    MaterialAutoCompleteTextView editListItem;

    @InjectView(R.id.text_last_edited)
    AdvancedTextView textLastEdited;

    OnFragmentInteractionListener mListener;

    private int bottomMargin = 0;

    private FridgeApplication application;

    public NotesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NotesFragment.
     */
    public static NotesFragment getInstance(int bottomMargin) {

        if (instance == null) {
            synchronized (NotesFragment.class) {
                if (instance == null) {
                    instance = new NotesFragment();
                    instance.bottomMargin = bottomMargin;
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // You can't use Fragment.setRetainInstance because he's meant only to fragments that aren't on the back stack.
        // setRetainInstance(true);
        getLoaderManager().initLoader(0, null, this);
        // add options menu
        setHasOptionsMenu(true);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            bottomMargin = savedInstanceState.getInt(KEY_CONTENT);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View containerView = inflater.inflate(R.layout.fragment_notes, container, false);

        // navigation bar height margin hack
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                containerView.findViewById(R.id.height_hack).getLayoutParams();
        params.height = (int) (bottomMargin * 0.85f);
        containerView.findViewById(R.id.height_hack).setLayoutParams(params);

        // inject and return the view
        ButterKnife.inject(this, containerView);
        return containerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // set recycler view item animator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        textListName.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!TextUtils.isEmpty(textListName.getText())) {
                        // set the new list name in the prefs store
                        application.prefStore.set(SharedPrefStore.Pref.SHOPPING_LIST_NAME,
                                textListName.getText().toString().trim());
                        KeyboardUtils.hideSoftKeyboard(textListName);
                        textListName.clearFocus();
                        // update last edited timestamp
                        updateLastEditedTimestamp();
                    } else {
                        textListName.setError(application.getString(R.string.error_no_list_name));
                        textListName.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        // add a new note on IME_ACTION_DONE
        editListItem.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!TextUtils.isEmpty(editListItem.getText())) {
                        // save the note
                        Calendar calendar = Calendar.getInstance();
                        NoteItem noteItem = new NoteItem(editListItem.getText().toString().trim(),
                                calendar.getTimeInMillis() / 1000, false, false);
                        noteItem.setItemId(noteItem.hashCode());
                        noteItem.save();
                        addItemOrRestartLoader();
                        ((NotesAdapter) recyclerView.getAdapter()).addItem(-1, noteItem);
                        KeyboardUtils.hideSoftKeyboard(editListItem);
                        editListItem.setText("");
                        editListItem.clearFocus();
                        // update last edited timestamp
                        updateLastEditedTimestamp();
                    } else {
                        editListItem.setError(application.getString(R.string.error_no_item_name));
                        editListItem.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        // we must have a text change listener to clear the error if any is set
        textListName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isAdded()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                textListName.setError(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 50);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editListItem.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                editListItem.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setShoppingListName();
        setLastEditedTimestamp();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        application = ((FridgeApplication) activity.getApplication());
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, bottomMargin);
    }

    @Override
    public void onResume() {

        ((MainActivity) getActivity()).showContextMenu(false);
        ((MainActivity) getActivity()).setActionBarIcon(R.drawable.ic_arrow_back);
        ((MainActivity) getActivity()).setActionBarTitle(application.getString(R.string.notes)
                .toLowerCase(Locale.ENGLISH));
        setLastEditedTimestamp();
        setShoppingListName();
        super.onResume();
    }

    @Override
    public Loader<List<NoteItem>> onCreateLoader(int id, Bundle args) {

        return new NotesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<NoteItem>> loader, List<NoteItem> data) {

        Log.d(Log.TAG, "onLoadFinished");
        if (data != null && data.size() > 0) {
            // hide the empty text
            emptyText.setVisibility(View.GONE);

            // set the adapter
            NotesAdapter adapter = new NotesAdapter((ArrayList<NoteItem>) data, application,
                    getActivity(), this);
            recyclerView.setAdapter(adapter);
        } else {
            // the list view cannot take a null pointer as an adapter
            // use an empty list instead
            NotesAdapter adapter = new NotesAdapter(new ArrayList<NoteItem>(), application,
                    getActivity(), this);
            recyclerView.setAdapter(adapter);
            // show the empty view
            emptyText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NoteItem>> loader) {
        // remove the references to the previous adapter
        try {
            NotesAdapter adapter = new NotesAdapter(new ArrayList<NoteItem>(), application,
                    getActivity(), this);
            recyclerView.setAdapter(adapter);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_notes, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            // Toast.makeText(getActivity(), "Back", Toast.LENGTH_SHORT).show();
            Log.d(Log.TAG, "Back");
        } else if (id == R.id.action_clear_notes) {
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
            builder.setTitle(application.getString(R.string.clear_notes));
            builder.setMessage(application.getString(R.string.clear_notes_confirmation))
                    .setPositiveButton(application.getString(R.string.delete),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {
                                    // FIRE ZE MISSILES!
                                    new Delete().from(NoteItem.class).execute();
                                    ((NotesAdapter) recyclerView.getAdapter()).clearAllNotes();
                                    // notes are deleted, restart the loader
                                    addItemOrRestartLoader();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    dialog.dismiss();
                                }
                            });
            // Create the AlertDialog object and return it
            builder.create().show();
        } else if (id == R.id.action_share_notes) {
            // if there are unchecked notes, create a send intent
            List<NoteItem> notes = new Select().from(NoteItem.class)
                    .where("is_checked = ?", false)
                    .and("status != ?", true)
                    .execute();
            if (notes.size() > 0) {
                // generate text from unchecked notes
                StringBuilder stringBuilder = new StringBuilder();
                for (NoteItem note : notes) {
                    stringBuilder.append(note.getNote()).append("\n***\n");
                }
                // remove the extra chars in the end
                stringBuilder.delete(stringBuilder.length() - 5, stringBuilder.length());

                // create the send intent
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                        application.prefStore.getString(SharedPrefStore.Pref.SHOPPING_LIST_NAME));
                sendIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            } else {
                SnackBar snackBar = new SnackBar(getActivity(),
                        "You must have unchecked notes to generate a message.");
                snackBar.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void addItemOrRestartLoader() {

        if (isAdded()) {
            Log.d(Log.TAG, "addItemOrRestartLoader called");
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    private void updateLastEditedTimestamp() {

        Calendar calendar = Calendar.getInstance();
        long editedTimestamp = calendar.getTimeInMillis() / 1000;
        application.prefStore.set(SharedPrefStore.Pref.SHOPPING_LIST_LAST_EDITED,
                String.valueOf(editedTimestamp));
        if (isAdded()) {
            setLastEditedTimestamp();
        }
    }

    private void setLastEditedTimestamp() {

        if (textLastEdited != null) {
            if (TextUtils.isEmpty(application.prefStore.getString(
                    SharedPrefStore.Pref.SHOPPING_LIST_LAST_EDITED))) {
                textLastEdited.setText(String.format(application.getString(R.string.last_edited),
                        application.getString(R.string.never)));
            } else {
                Calendar calendar = Calendar.getInstance();
                long currentTimestamp = calendar.getTimeInMillis() / 1000;
                long listTimestamp = Long.parseLong(application.prefStore.getString(
                        SharedPrefStore.Pref.SHOPPING_LIST_LAST_EDITED));

                // get today's timestamp
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 1);
                long todayTimestamp = calendar.getTimeInMillis() / 1000;

                String relativeTimestamp;
                // check if the list change timestamp is from today
                if (listTimestamp > todayTimestamp) {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                    relativeTimestamp = dateFormat.format(new Date(listTimestamp * 1000));
                } else {
                    relativeTimestamp = TimeSpans
                            .getRelativeTimeSince(listTimestamp * 1000, currentTimestamp * 1000);
                }
                textLastEdited.setText(String.format(application.getString(R.string.last_edited),
                        relativeTimestamp));
            }
        }
    }

    private void setShoppingListName() {
        // set the shopping list name from prefs if it is saved
        if (textListName != null) {
            if (!TextUtils.isEmpty(
                    application.prefStore.getString(SharedPrefStore.Pref.SHOPPING_LIST_NAME))) {
                textListName.setText(
                        application.prefStore.getString(SharedPrefStore.Pref.SHOPPING_LIST_NAME));
            }
        }
    }

    @Override
    public void onNoteChange() {

        updateLastEditedTimestamp();
    }
}
