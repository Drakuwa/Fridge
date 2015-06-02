package com.app.afridge.ui.fragments;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.adapters.HistoryAdapter;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.HistoryItem;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.loaders.HistoryLoader;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.FileUtils;
import com.app.afridge.utils.Log;
import com.app.afridge.views.AdvancedTextView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Fridge items - A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Created by drakuwa on 1/28/15.
 */
public class HistoryFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<HistoryItem>>,
        DatePickerDialog.OnDateSetListener {

    private static final String KEY_CONTENT = "HistoryFragment:Content";

    // Singleton
    private static volatile HistoryFragment instance = null;

    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;

    @InjectView(android.R.id.empty)
    LinearLayout emptyText;

    @InjectView(R.id.text_filter)
    AdvancedTextView textFilter;

    OnFragmentInteractionListener mListener;

    private int bottomMargin = 0;

    private FridgeApplication application;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HistoryFragment.
     */
    public static HistoryFragment getInstance(int bottomMargin) {

        if (instance == null) {
            synchronized (HistoryFragment.class) {
                if (instance == null) {
                    instance = new HistoryFragment();
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
        getLoaderManager().initLoader(0, new Bundle(), this);
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
        View containerView = inflater.inflate(R.layout.fragment_history, container, false);

        // navigation bar height margin hack
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) containerView
                .findViewById(R.id.height_hack).getLayoutParams();
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
        ((MainActivity) getActivity()).setActionBarTitle(
                application.getString(R.string.history).toLowerCase(Locale.ENGLISH));
        textFilter.setText(getString(R.string.no_filter).toUpperCase(Locale.US));
        super.onResume();
    }

    @Override
    public Loader<List<HistoryItem>> onCreateLoader(int id, Bundle args) {

        if (args.containsKey(Constants.EXTRA_FILTER_TYPE)) {
            return new HistoryLoader(getActivity(), args.getLong(Constants.EXTRA_FILTER_TYPE));
        }
        return new HistoryLoader(getActivity(), 0);
    }

    @Override
    public void onLoadFinished(Loader<List<HistoryItem>> loader, List<HistoryItem> data) {

        Log.d(Log.TAG, "onLoadFinished");
        if (data != null && data.size() > 0) {
            // hide the empty text
            emptyText.setVisibility(View.GONE);

            // set the adapter
            HistoryAdapter adapter = new HistoryAdapter((ArrayList<HistoryItem>) data, application);
            recyclerView.setAdapter(adapter);
        } else {
            // the list view cannot take a null pointer as an adapter
            // use an empty list instead
            HistoryAdapter adapter = new HistoryAdapter(new ArrayList<HistoryItem>(), application);
            recyclerView.setAdapter(adapter);
            // show the empty view
            emptyText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<HistoryItem>> loader) {
        // remove the references to the previous adapter
        try {
            HistoryAdapter adapter = new HistoryAdapter(new ArrayList<HistoryItem>(), application);
            recyclerView.setAdapter(adapter);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_history, menu);
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
        } else if (id == R.id.action_clear_history) {
            // material design library dialog implementation - not impressive :)
            // Dialog dialog = new Dialog(getActivity(), "Clear history?", application.getString(R.string.clear_history_confirmation));
            // dialog.addCancelButton(application.getString(android.R.string.cancel));
            // dialog.show();
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
            builder.setTitle(application.getString(R.string.clear_history));
            builder.setMessage(application.getString(R.string.clear_history_confirmation))
                    .setPositiveButton(application.getString(R.string.delete),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {
                                    // FIRE ZE MISSILES!
                                    new Delete().from(HistoryItem.class).execute();
                                    ((HistoryAdapter) recyclerView.getAdapter()).clearAllHistory();
                                    // delete all the images that are kept with removed items
                                    List<FridgeItem> items = new Select().from(FridgeItem.class)
                                            .where("status = ?", true).execute();
                                    for (FridgeItem item : items) {
                                        if (!TextUtils.isDigitsOnly(item.getType())) {
                                            // probably a file, try to delete it
                                            File itemType = new File(item.getType());
                                            if (itemType.exists()) {
                                                FileUtils.deleteFile(itemType);
                                            }
                                        }
                                    }
                                    // delete all fridge items that are marked as removed, but still kept in DB
                                    new Delete().from(FridgeItem.class).where("status = ?", true)
                                            .execute();
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
        } else if (id == R.id.action_filter_history) {
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
            builder.setTitle(R.string.since)
                    .setItems(R.array.date_array, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            switch (which) {
                                case 0:
                                    // today
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                                    calendar.set(Calendar.MINUTE, 0);
                                    calendar.set(Calendar.SECOND, 1);
                                    Log.d(Log.TAG, application.dateFormat
                                            .format(new Date(calendar.getTimeInMillis())));
                                    restartLoader(calendar.getTimeInMillis() / 1000);
                                    textFilter.setText(
                                            getString(R.string.today).toUpperCase(Locale.US));
                                    break;
                                case 1:
                                    // yesterday
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.HOUR_OF_DAY, 0);
                                    cal.set(Calendar.MINUTE, 0);
                                    cal.set(Calendar.SECOND, 1);
                                    cal.add(Calendar.DATE, -1);
                                    Log.d(Log.TAG, application.dateFormat
                                            .format(new Date(cal.getTimeInMillis())));
                                    restartLoader(cal.getTimeInMillis() / 1000);
                                    textFilter.setText(
                                            getString(R.string.yesterday).toUpperCase(Locale.US));
                                    break;
                                case 2:
                                    // get the current date to set as default
                                    final Calendar c = Calendar.getInstance();
                                    int year = c.get(Calendar.YEAR);
                                    int month = c.get(Calendar.MONTH);
                                    int day = c.get(Calendar.DAY_OF_MONTH);
                                    // choose date
                                    Bundle b = new Bundle();
                                    b.putInt(DatePickerDialogFragment.YEAR, year);
                                    b.putInt(DatePickerDialogFragment.MONTH, month);
                                    b.putInt(DatePickerDialogFragment.DATE, day);
                                    DatePickerDialogFragment picker
                                            = new DatePickerDialogFragment();
                                    picker.setArguments(b);
                                    picker.setListener(HistoryFragment.this);
                                    picker.show(getActivity().getSupportFragmentManager(),
                                            "frag_date_picker");
                                    break;
                                case 3:
                                    // clear filter
                                    restartLoader(0);
                                    textFilter.setText(
                                            getString(R.string.no_filter).toUpperCase(Locale.US));
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
            builder.create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // Do something with the date chosen by the user
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        Log.d(Log.TAG, application.dateFormat.format(new Date(calendar.getTimeInMillis())));
        restartLoader(calendar.getTimeInMillis() / 1000);
        textFilter.setText(application.dateFormat.format(new Date(calendar.getTimeInMillis()))
                .toUpperCase(Locale.US));
    }

    private void restartLoader(long filterTimestamp) {

        if (isAdded()) {
            Bundle args = new Bundle();
            if (filterTimestamp != 0) {
                args.putLong(Constants.EXTRA_FILTER_TYPE, filterTimestamp);
            }
            getLoaderManager().restartLoader(0, args, this);
        }
    }
}
