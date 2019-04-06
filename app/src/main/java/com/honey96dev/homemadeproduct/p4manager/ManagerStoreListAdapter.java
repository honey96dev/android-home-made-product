package com.honey96dev.homemadeproduct.p4manager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honey96dev.homemadeproduct.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ManagerStoreListAdapter extends RecyclerView.Adapter<ManagerStoreListAdapter.ManagerStoreViewHolder> {
    Context mContext;
    ArrayList<ManagerStore> mDataset;

    // Dataitem
    public static class ManagerStore {
        public String id;
        public String name;
        public String description;
        public String icon;
        public String likes;
        public ManagerStore(String id, String name, String description, String icon, String likes) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.likes = likes;
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ManagerStoreViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView imageView;
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView likesTextView;

        public ManagerStoreViewHolder(View root) {
            super(root);
            imageView = (ImageView) root.findViewById(R.id.image_view);
            titleTextView = (TextView) root.findViewById(R.id.title_text_view);
            descriptionTextView = (TextView) root.findViewById(R.id.description_text_view);
            likesTextView = (TextView) root.findViewById(R.id.likesTextView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ManagerStoreListAdapter(Context context, ArrayList<ManagerStore> dataset) {
        mContext = context;
        mDataset = dataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ManagerStoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_manager_store_item, parent, false);

        return new ManagerStoreViewHolder(root);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ManagerStoreViewHolder holder, int position) {
        final ManagerStore store = mDataset.get(position);
        try {
            Picasso.get().load(store.icon)
                    .into(holder.imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.titleTextView.setText(store.name);
        holder.descriptionTextView.setText(store.description);
        holder.likesTextView.setText(store.likes);
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ManagerProductListActivity.class);
                intent.putExtra(ManagerProductListFragment.STORE_ID_KEY, store.id);
                mContext.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
