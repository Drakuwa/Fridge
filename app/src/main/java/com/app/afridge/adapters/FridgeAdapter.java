package com.app.afridge.adapters;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.ChangeType;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.HistoryItem;
import com.app.afridge.dom.ItemType;
import com.app.afridge.interfaces.ClickListener;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.ui.fragments.ItemDetailsFragment;
import com.app.afridge.utils.AnimationsController;
import com.app.afridge.utils.CircleTransform;
import com.app.afridge.utils.Common;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.Log;
import com.app.afridge.views.AdvancedTextView;
import com.gc.materialdesign.widgets.SnackBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Recycler view adapter for fridge items
 * <p/>
 * Created by drakuwa on 2/12/15.
 */
public class FridgeAdapter extends RecyclerView.Adapter<FridgeAdapter.ViewHolder> {

  private ArrayList<FridgeItem> items;
  private FridgeApplication application;
  private MainActivity activity;
  private int bottomMargin;

  public FridgeAdapter(ArrayList<FridgeItem> items, final FridgeApplication application, MainActivity activity, int bottomMargin) {

    this.items = items;
    this.application = application;
    this.activity = activity;
    this.bottomMargin = bottomMargin;

    // sort the items by expiration date
    Collections.sort(this.items, new Comparator<FridgeItem>() {

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
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        return lhsMillis < rhsMillis ? -1 : (lhsMillis == rhsMillis ? 0 : 1);
      }
    });
  }

  // Create new views (invoked by the layout manager)
  @Override
  public FridgeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    // create a new view
    // View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fridge, parent, false);
    // set the view's size, margins, padding and layout parameters
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fridge, parent, false));

  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(final FridgeAdapter.ViewHolder holder, final int position) {
    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    final FridgeItem item = items.get(position);

    // bind item to view holder
    // holder.bindItem(item);

    // set the item name
    holder.textName.setText(item.getName());

    // check if the type of the item is one of the predefined, or a file
    // and set the image accordingly - find a better way!
    File itemType = new File(item.getType());
    Picasso loader = Picasso.with(application.getApplicationContext());
    RequestCreator requestCreator;
    if (TextUtils.isDigitsOnly(item.getType())) {
      requestCreator = loader.load(ItemType.DRAWABLES[Integer.parseInt(item.getType())]);
    }
    else {
      requestCreator = loader.load(itemType);
    }
    requestCreator.resize(application.screenWidth / 2, application.screenWidth / 2)
            .centerInside()
            .transform(new CircleTransform())
            .error(R.drawable.fridge_placeholder)
            .into(holder.image);

    // set the expiration date label
    if (item.getExpirationDate() != 0) {
      holder.textExpirationDate.setText(Common.getTimestamp(item, application));
      holder.textExpirationDate.setVisibility(View.VISIBLE);
    }
    else {
      holder.textExpirationDate.setVisibility(View.GONE);
    }

    // set the item click listener
    holder.setClickListener(new ClickListener() {

      @Override
      public void onClick(View v, final int pos, boolean isLongClick) {

        if (isLongClick) {
          // View v at position pos is long-clicked.
          Log.d(Log.TAG, "item is long-clicked");
        }
        else {
          if (v instanceof ImageView) {
            // delete item it set to true, unless it gets switched by clicking on UNDO
            final boolean[] deleteItem = {true};
            // hack ;) if the clicked view is an ImageView - it has to be the delete view
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(activity);
            builder.setMessage(String.format(application.getString(R.string.delete_out_confirmation), item.getName()))
                    .setPositiveButton(application.getString(R.string.delete), new DialogInterface.OnClickListener() {

                      public void onClick(DialogInterface dialog, int id) {

                        SnackBar snackBar = new SnackBar(activity,
                                String.format(application.getString(R.string.item_deleted), item.getName()),
                                application.getString(R.string.undo), new View.OnClickListener() {

                          @Override
                          public void onClick(View v) {
                            // cancel deletion and re-insert the item
                            deleteItem[0] = false;
                            addItem(pos, item);
                          }
                        });
                        snackBar.setOnhideListener(new SnackBar.OnHideListener() {

                          @Override
                          public void onHide() {

                            if (deleteItem[0]) {
                              // save the delete in history
                              HistoryItem historyItem = new HistoryItem(item,
                                      Calendar.getInstance().getTimeInMillis() / 1000, ChangeType.DELETE);
                              historyItem.save();
                              // FIRE ZE MISSILES!
                              item.setRemoved(true);
                              item.save();
                            }
                          }
                        });
                        snackBar.show();
                        removeItem(pos);
                      }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                      public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                      }
                    });
            // Create the AlertDialog object and return it
            builder.create().show();
            return;
          }

          // View v at position pos is clicked.
          // get the fragment container
          View containerView = activity.findViewById(R.id.container);

          // get a fragment transaction object
          FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
          fragmentTransaction.addToBackStack(null);

          Fragment fragment = ItemDetailsFragment.getInstance(bottomMargin);
          int totalHeight, totalWidth;

          // set the item id
          Bundle args = new Bundle();
          args.putInt(Constants.EXTRA_ITEM_ID, item.getItemId());
          fragment.setArguments(args);

          // use fragment transitions if we are on Lollipop or higher
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //                    setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_transform));
            //                    setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));

            // TODO                    setSharedElementEnterTransition(new ChangeBounds());
            //                    setSharedElementReturnTransition(new ChangeBounds());
            //                    setAllowEnterTransitionOverlap(true);
            //                    setAllowReturnTransitionOverlap(true);

            fragment.setSharedElementEnterTransition(TransitionInflater.from(activity).inflateTransition(R.transition.change_image_transform));
            fragment.setEnterTransition(TransitionInflater.from(activity).inflateTransition(R.transition.change_image_transform));

            fragmentTransaction
                    .replace(R.id.container, fragment)
                    .addSharedElement(holder.image, activity.getString(R.string.shared_image_transition))
                    .addSharedElement(holder.textName, activity.getString(R.string.shared_name_transition))
                    .commit();
          }
          else {
            // if we have an item details transaction, set the fragment size to match the clicked view
            totalHeight = containerView.getMeasuredHeight();
            totalWidth = containerView.getMeasuredWidth();
            //                    ((ItemDetailsFragment)fragment).show(fragmentTransaction, "item_details");

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) containerView.getLayoutParams();
            params.width = v.getWidth();
            params.height = v.getHeight();
            containerView.setLayoutParams(params);

            fragmentTransaction
                    .setCustomAnimations(0, 0)
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_NONE)
                    .commit();

            // if we are on older version, use the custom expend animation
            AnimationsController.expandUp(v, containerView, totalHeight, totalWidth);
          }
        }
      }
    });
  }

  // Return the size of your dataset (invoked by the layout manager)
  @Override
  public int getItemCount() {

    return items.size();
  }

  public void addItem(int position, FridgeItem item) {

    items.add(position, item);
    notifyItemInserted(position);
  }

  public void removeItem(int position) {

    items.remove(position);
    notifyItemRemoved(position);
  }

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  // Our ViewHolder now implements OnClickListener and OnLongClickListener.
  public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    // private FridgeItem item;
    private ClickListener clickListener;
    View root;

    // each data item is just a string in this case
    @InjectView(R.id.text_name)
    AdvancedTextView textName;
    @InjectView(R.id.image_item)
    ImageView image;
    @InjectView(R.id.image_delete)
    ImageView imageDelete;
    @InjectView(R.id.text_expiration)
    AdvancedTextView textExpirationDate;

    public ViewHolder(View view) {

      super(view);
      root = view;
      ButterKnife.inject(this, view);

      itemView.setOnClickListener(this);
      itemView.setOnLongClickListener(this);
      imageDelete.setOnClickListener(this);
    }

    // public void bindItem(FridgeItem item) {
    //     this.item = item;
    // }

    /* Setter for listener. */
    public void setClickListener(ClickListener clickListener) {

      this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {

      // If not long clicked, pass last variable as false.
      clickListener.onClick(v, getPosition(), false);
    }

    @Override
    public boolean onLongClick(View v) {

      // If long clicked, passed last variable as true.
      clickListener.onClick(v, getPosition(), true);
      return true;
    }
  }
}
