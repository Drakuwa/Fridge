package com.app.afridge.ui.fragments;

import com.activeandroid.query.Select;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.adapters.FridgeAdapter;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.interfaces.Screenshotable;
import com.app.afridge.loaders.FridgeItemLoader;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.Log;
import com.melnykov.fab.FloatingActionButton;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * Fridge items - A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Created by drakuwa on 1/28/15.
 */
public class FridgeFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<FridgeItem>>, Screenshotable {

    private static final String KEY_CONTENT = "FridgeFragment:Content";

    // Singleton
    private static volatile FridgeFragment instance = null;

    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;

    @InjectView(android.R.id.empty)
    LinearLayout emptyText;

    @InjectView(R.id.button_new)
    FloatingActionButton buttonNew;

    private int bottomMargin = 0;

    private FridgeApplication application;

    private Bitmap bitmap;

    private View containerView;

    private OnFragmentInteractionListener mListener;

    private FridgeAdapter adapter;

    public FridgeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FridgeFragment.
     */
    public static FridgeFragment getInstance(int bottomMargin) {

        if (instance == null) {
            synchronized (FridgeFragment.class) {
                if (instance == null) {
                    instance = new FridgeFragment();
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

        // check if we have arguments
        if (savedInstanceState == null && getArguments() != null) {
            // get the extras
            // Bundle args = getArguments();
            // isDatabaseChanged = args.getBoolean(Constants.EXTRA_RESTART_LOADER);
        }

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            bottomMargin = savedInstanceState.getInt(KEY_CONTENT);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        containerView = inflater.inflate(R.layout.fragment_fridge, container, false);

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

        // attach FAB to grid view
        buttonNew.attachToRecyclerView(recyclerView);

        if (new Select().from(FridgeItem.class).execute().size() == 0) {
            // no fridge items...
            Log.d(Log.TAG, "no fridge items...");
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // set recycler view item animator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2,
                GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fridge, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (adapter != null) {
                    adapter.getFilter().filter(query);
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                // do s.th.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.button_new)
    public void addNewItem(View view) {
        // Save Grid state
        // itemGridState = itemGrid.onSaveInstanceState();

        takeScreenShot();
        ((MainActivity) getActivity()).setScreenshotable(this);

        Rect rect = new Rect();
        view.getLocalVisibleRect(rect);

        Point buttonCenter = new Point((int) view.getX() + view.getWidth() / 2,
                (int) view.getY() + view.getHeight() / 2);
        mListener.onFragmentInteraction(true, buttonCenter,
                AddItemFragment.class.getCanonicalName());
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
        // restart loader only on DB change
        if (((MainActivity) getActivity()).isDatabaseChanged()) {
            addItemOrRestartLoader(null);
            ((MainActivity) getActivity()).setDatabaseChanged(false);
        }
        ((MainActivity) getActivity()).showContextMenu(true);
        ((MainActivity) getActivity()).setActionBarIcon(R.mipmap.ic_launcher);
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.app_name).toLowerCase(Locale.ENGLISH));
        super.onResume();
    }

    @Override
    public Loader<List<FridgeItem>> onCreateLoader(int id, Bundle args) {

        return new FridgeItemLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<FridgeItem>> loader, List<FridgeItem> data) {

        Log.d(Log.TAG, "onLoadFinished");
        if (data != null && data.size() > 0) {
            // hide the empty text
            emptyText.setVisibility(View.GONE);

            // set the adapter
            adapter = new FridgeAdapter((ArrayList<FridgeItem>) data, application,
                    (MainActivity) getActivity(), bottomMargin);
            recyclerView.setAdapter(adapter);

            // restore view state
            //            if (itemGridState != null) {
            //                Log.d(Log.TAG, "onRestoreInstanceState");
            //                itemGrid.onRestoreInstanceState(itemGridState);
            //                itemGridState = null;
            //            }
            // scroll list to bottom
            if (((MainActivity) getActivity()).isDatabaseChanged()) {
                if (recyclerView.getChildCount() > 0) {
                    recyclerView.scrollToPosition(recyclerView.getChildCount() - 1);
                }
            }
        } else {
            // the list view cannot take a null pointer as an adapter
            // use an empty list instead
            adapter = new FridgeAdapter(new ArrayList<FridgeItem>(), application,
                    (MainActivity) getActivity(), bottomMargin);
            recyclerView.setAdapter(adapter);
            // show the empty view
            emptyText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<FridgeItem>> loader) {
        // remove the references to the previous adapter
        try {
            adapter = new FridgeAdapter(new ArrayList<FridgeItem>(), application,
                    (MainActivity) getActivity(), bottomMargin);
            recyclerView.setAdapter(adapter);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public void addItemOrRestartLoader(FridgeItem fridgeItem) {

        if (isAdded()) {
            Log.d(Log.TAG, "addItemOrRestartLoader called: " + fridgeItem);
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public void takeScreenShot() {

        new Thread() {

            @Override
            public void run() {

                try {
                    Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                            containerView.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    containerView.draw(canvas);
                    FridgeFragment.this.bitmap = bitmap;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public Bitmap getBitmap() {

        return bitmap;
    }
}
