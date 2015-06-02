package com.app.afridge.adapters;

import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.enums.ItemType;
import com.app.afridge.utils.CircleTransform;
import com.app.afridge.views.AdvancedTextView;
import com.squareup.picasso.Picasso;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Fridge items grid adapter
 * <p/>
 * Created by drakuwa on 1/28/15.
 */
public class FridgeItemAdapter extends BaseAdapter {

    private final LayoutInflater inflater;

    private ArrayList<FridgeItem> items;

    private FridgeApplication application;

    public FridgeItemAdapter(ArrayList<FridgeItem> items, final FridgeApplication application) {

        this.items = items;
        this.application = application;
        this.inflater = LayoutInflater.from(application.getApplicationContext());

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return lhsMillis > rhsMillis ? -1 : (lhsMillis == rhsMillis ? 0 : 1);
            }
        });
    }

    @Override
    public int getCount() {

        return items.size();
    }

    @Override
    public FridgeItem getItem(int position) {

        return items.get(position);
    }

    @Override
    public long getItemId(int position) {

        return items.get(position).getItemId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // standard view holder pattern, inflate only if needed, re-use otherwise
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_fridge, parent, false);

            ViewHolder viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final FridgeItem item = getItem(position);

        holder.textName.setText(item.getName());
        File itemType = new File(item.getType());
        if (itemType.exists()) {
            Picasso.with(application.getApplicationContext())
                    .load(itemType)
                    .resize(application.screenWidth / 2, application.screenWidth / 2)
                    .centerInside()
                    .transform(new CircleTransform())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.image);
        } else if (TextUtils.isDigitsOnly(item.getType())) {
            Picasso.with(application.getApplicationContext())
                    .load(ItemType.DRAWABLES[Integer.parseInt(item.getType())])
                            // .centerInside()
                    .transform(new CircleTransform())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.image);
        } else {
            // we have a missing path?
            try {
                Picasso.with(application.getApplicationContext())
                        .load(itemType)
                        .resize(application.screenWidth / 2, application.screenWidth / 2)
                        .centerInside()
                        .transform(new CircleTransform())
                        .error(R.mipmap.ic_launcher)
                        .into(holder.image);
            } catch (Exception ignored) {
            }
        }

        return convertView;
    }

    static class ViewHolder {

        @InjectView(R.id.text_name)
        AdvancedTextView textName;

        @InjectView(R.id.image_item)
        ImageView image;

        public ViewHolder(View view) {

            ButterKnife.inject(this, view);
        }
    }
}
