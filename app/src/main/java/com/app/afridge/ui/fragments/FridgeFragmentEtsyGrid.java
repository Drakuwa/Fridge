package com.app.afridge.ui.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.transition.ChangeBounds;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.activeandroid.query.Select;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.adapters.FridgeItemAdapter;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.interfaces.Screenshotable;
import com.app.afridge.loaders.FridgeItemLoader;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.AnimationsController;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.Log;
import com.app.afridge.views.AdvancedTextView;
import com.etsy.android.grid.StaggeredGridView;
import com.melnykov.fab.FloatingActionButton;

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
public class FridgeFragmentEtsyGrid extends Fragment implements LoaderManager.LoaderCallbacks<List<FridgeItem>>, Screenshotable {

  @InjectView(R.id.grid_view)
  StaggeredGridView itemGrid;
  @InjectView(android.R.id.empty)
  LinearLayout emptyText;
  @InjectView(R.id.button_new)
  FloatingActionButton buttonNew;

  private static final String KEY_CONTENT = "FridgeFragment:Content";
  private int bottomMargin = 0;
  private FridgeApplication application;
  private Bitmap bitmap;
  private View containerView;
  // private static Parcelable itemGridState = null;
  // private int scrollX, scrollY;

  private OnFragmentInteractionListener mListener;

  // Singleton
  private static volatile FridgeFragmentEtsyGrid instance = null;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment FridgeFragment.
   */
  public static FridgeFragmentEtsyGrid getInstance(int bottomMargin) {

    if (instance == null) {
      synchronized (FridgeFragmentEtsyGrid.class) {
        if (instance == null) {
          instance = new FridgeFragmentEtsyGrid();
          instance.bottomMargin = bottomMargin;
        }
      }
    }
    return instance;
  }

  public FridgeFragmentEtsyGrid() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    // You can't use Fragment.setRetainInstance because he's meant only to fragments that aren't on the back stack.
    // setRetainInstance(true);
    getLoaderManager().initLoader(0, null, this);

    // check if we have arguments
    if (savedInstanceState == null && getArguments() != null) {
      // add options menu
      setHasOptionsMenu(true);
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
    containerView = inflater.inflate(R.layout.fragment_fridge_etsy, container, false);

    // navigation bar height margin hack
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) containerView.findViewById(R.id.height_hack).getLayoutParams();
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
    buttonNew.attachToListView(itemGrid);

    if (new Select().from(FridgeItem.class).execute().size() == 0) {
      // no fridge items...
    }

    // set grid item click listener
    itemGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        //                Intent i = new Intent(getActivity(), ItemDetailsActivity.class);
        //                Bundle b = null;
        //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        //                    // b = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(),
        //                    //                                         view.getHeight()).toBundle();
        //                    // Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //                    Bitmap bitmap = Common.getBitmapFromView(view);
        //                    b = ActivityOptions.makeThumbnailScaleUpAnimation(view, bitmap, 0, 0).toBundle();
        //                }
        //                getActivity().startActivity(i, b);
        //                mListener.onFragmentInteractionFallback(true, view, ItemDetailsFragment.class.getCanonicalName(),
        //                        ((FridgeItem) parent.getItemAtPosition(position)).getItemId());

        // Save Grid state
        // TODO itemGridState = itemGrid.onSaveInstanceState();

        // get the fragment container
        View containerView = getActivity().findViewById(R.id.container);

        // get a fragment transaction object
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);

        Fragment fragment = ItemDetailsFragment.getInstance(bottomMargin);
        int totalHeight, totalWidth;

        // set the item id
        Bundle args = new Bundle();
        args.putInt(Constants.EXTRA_ITEM_ID, ((FridgeItem) itemGrid.getItemAtPosition(position)).getItemId());
        fragment.setArguments(args);

        // use fragment transitions if we are on Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          //                    setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_transform));
          //                    setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));

          setSharedElementEnterTransition(new ChangeBounds());
          setSharedElementReturnTransition(new ChangeBounds());
          setAllowEnterTransitionOverlap(true);
          setAllowReturnTransitionOverlap(true);

          fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_transform));
          fragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_transform));

          // Our shared elements
          ImageView itemImage = (ImageView) view.findViewById(R.id.image_item);
          AdvancedTextView itemName = (AdvancedTextView) view.findViewById(R.id.text_name);

          fragmentTransaction
                  .replace(R.id.container, fragment)
                  .addSharedElement(itemImage, getString(R.string.shared_image_transition))
                  .addSharedElement(itemName, getString(R.string.shared_name_transition))
                  .commit();
        }
        else {
          // if we have an item details transaction, set the fragment size to match the clicked view
          totalHeight = containerView.getMeasuredHeight();
          totalWidth = containerView.getMeasuredWidth();
          //                    ((ItemDetailsFragment)fragment).show(fragmentTransaction, "item_details");

          FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) containerView.getLayoutParams();
          params.width = view.getWidth();
          params.height = view.getHeight();
          containerView.setLayoutParams(params);

          fragmentTransaction
                  .setCustomAnimations(0, 0)
                  .replace(R.id.container, fragment)
                  .setTransition(FragmentTransaction.TRANSIT_NONE)
                  .commit();

          // if we are on older version, use the custom expend animation
          AnimationsController.expandUp(view, containerView, totalHeight, totalWidth);
        }
      }
    });
  }

  @OnClick(R.id.button_new)
  public void addNewItem(View view) {
    // Save Grid state
    // TODO itemGridState = itemGrid.onSaveInstanceState();

    takeScreenShot();
    ((MainActivity) getActivity()).setScreenshotable(this);

    Rect rect = new Rect();
    view.getLocalVisibleRect(rect);

    Point buttonCenter = new Point((int) view.getX() + view.getWidth() / 2,
            (int) view.getY() + view.getHeight() / 2);
    mListener.onFragmentInteraction(true, buttonCenter, AddItemFragment.class.getCanonicalName());
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
    }
    catch (ClassCastException e) {
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
    ((MainActivity) getActivity()).setActionBarIcon(R.mipmap.ic_launcher);
    ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_name).toLowerCase(Locale.ENGLISH));
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
      FridgeItemAdapter adapter = new FridgeItemAdapter((ArrayList<FridgeItem>) data, application);
      itemGrid.setAdapter(adapter);

      // TODO restore view state
      //            if (itemGridState != null) {
      //                Log.d(Log.TAG, "onRestoreInstanceState");
      //                itemGrid.onRestoreInstanceState(itemGridState);
      //                itemGridState = null;
      //            }
      // scroll list to bottom
      if (((MainActivity) getActivity()).isDatabaseChanged()) {
        if (itemGrid.getChildCount() > 0) {
          itemGrid.setSelection(itemGrid.getChildCount() - 1);
        }
      }
    }
    else {
      // the list view cannot take a null pointer as an adapter
      // use an empty list instead
      FridgeItemAdapter adapter = new FridgeItemAdapter(new ArrayList<FridgeItem>(), application);
      itemGrid.setAdapter(adapter);
      // show the empty view
      emptyText.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onLoaderReset(Loader<List<FridgeItem>> loader) {
    // remove the references to the previous adapter
    try {
      FridgeItemAdapter adapter = new FridgeItemAdapter(new ArrayList<FridgeItem>(), application);
      itemGrid.setAdapter(adapter);
    }
    catch (Exception ignored) {
      ignored.printStackTrace();
    }
  }

  public void addItemOrRestartLoader(FridgeItem fridgeItem) {

    if (isAdded()) {
      Log.d(Log.TAG, "addItemOrRestartLoader called");
      getLoaderManager().restartLoader(0, null, this);
    }
  }

  @Override
  public void takeScreenShot() {

    Thread thread = new Thread() {

      @Override
      public void run() {

        Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                containerView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        containerView.draw(canvas);
        FridgeFragmentEtsyGrid.this.bitmap = bitmap;
      }
    };
    thread.start();
  }

  @Override
  public Bitmap getBitmap() {

    return bitmap;
  }
}
