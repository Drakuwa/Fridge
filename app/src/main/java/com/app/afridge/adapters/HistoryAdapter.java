package com.app.afridge.adapters;

import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.HistoryItem;
import com.app.afridge.dom.enums.ItemType;
import com.app.afridge.interfaces.ClickListener;
import com.app.afridge.utils.CircleTransform;
import com.app.afridge.utils.Common;
import com.app.afridge.utils.Log;
import com.app.afridge.views.AdvancedTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Recycler view adapter for history items
 * <p/>
 * Created by drakuwa on 2/12/15.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private ArrayList<HistoryItem> items;

    private FridgeApplication application;

    public HistoryAdapter(ArrayList<HistoryItem> items, final FridgeApplication application) {

        this.items = items;
        this.application = application;

        // sort the items by expiration date
        Collections.sort(this.items, new Comparator<HistoryItem>() {

            @Override
            public int compare(HistoryItem lhs, HistoryItem rhs) {

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
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final HistoryAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final HistoryItem item = items.get(position);

        holder.textName.setText(item.getFridgeItem().getName());
        File itemType = new File(item.getFridgeItem().getType());

        Picasso loader = Picasso.with(application.getApplicationContext());
        RequestCreator requestCreator;
        if (TextUtils.isDigitsOnly(item.getFridgeItem().getType())) {
            requestCreator = loader
                    .load(ItemType.DRAWABLES[Integer.parseInt(item.getFridgeItem().getType())]);
        } else {
            requestCreator = loader.load(itemType);
        }
        requestCreator
                .resize((int) application.getResources().getDimension(R.dimen.history_item_logo),
                        (int) application.getResources().getDimension(R.dimen.history_item_logo))
                .centerInside()
                .transform(new CircleTransform())
                .error(R.mipmap.ic_launcher)
                .into(holder.image);

        // set the item type drawable
        switch (item.getChangeType()) {
            case ADD:
                loader.load(R.drawable.ic_add_history)
                        .error(R.mipmap.ic_launcher)
                        .into(holder.imageType);
                break;
            case MODIFY:
                loader.load(R.drawable.ic_edit_history)
                        .error(R.mipmap.ic_launcher)
                        .into(holder.imageType);
                break;
            case DELETE:
                loader.load(R.drawable.ic_delete_history)
                        .error(R.mipmap.ic_launcher)
                        .into(holder.imageType);
                break;
        }

        // set the timestamp
        holder.textTimestamp.setText(Common.getTimestamp(item, application));
        // holder.textTimestamp.setText(application.dateFormat.format(new Date(item.getTimestamp() * 1000)));

        holder.setClickListener(new ClickListener() {

            @Override
            public void onClick(View v, int pos, boolean isLongClick) {

                if (isLongClick) {
                    // View v at position pos is long-clicked.
                    Log.d(Log.TAG, "item is long-clicked");
                } else {
                    Log.d(Log.TAG, "item is clicked");
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return items.size();
    }

    @SuppressWarnings("unused")
    public void addItem(int position, HistoryItem item) {

        items.add(position, item);
        notifyItemInserted(position);
    }

    @SuppressWarnings("unused")
    public void removeItem(int position) {

        items.remove(position);
        notifyItemRemoved(position);
    }

    public void clearAllHistory() {

        items.clear();
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    // Our ViewHolder now implements OnClickListener and OnLongClickListener.
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        View root;

        // each data item is just a string in this case
        @InjectView(R.id.text_name)
        AdvancedTextView textName;

        @InjectView(R.id.text_timestamp)
        AdvancedTextView textTimestamp;

        @InjectView(R.id.image_item)
        ImageView image;

        @InjectView(R.id.image_type)
        ImageView imageType;

        // private HistoryItem item;
        private ClickListener clickListener;

        public ViewHolder(View view) {

            super(view);
            root = view;
            ButterKnife.inject(this, view);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

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
