package com.honey96dev.homemadeproduct.p4manager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honey96dev.homemadeproduct.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ManagerProductListAdapter extends RecyclerView.Adapter<ManagerProductListAdapter.ManagerProductViewHolder> {
    Context mContext;
    ArrayList<ManagerProduct> mDataset;

    // Dataitem
    public static class ManagerProduct {
        public String id;
        public String storeID;
        public String name;
        public String description;
        public String img1;
        public String img2;
        public String img3;
        public String img4;
        public String img5;
        public String img6;
        public String price;
        public String date;
        public ManagerProduct(String id, String storeID, String name, String desctiption,
                              String img1, String img2, String img3, String img4, String img5, String img6,
                              String price, String date) {
            this.id = id;
            this.storeID = storeID;
            this.name = name;
            this.storeID = storeID;
            this.description = desctiption;
            this.img1 = img1;
            this.img2 = img2;
            this.img3 = img3;
            this.img4 = img4;
            this.img5 = img5;
            this.img6 = img6;
            this.price = price;
            this.date = date;
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ManagerProductViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView imageView;
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView priceTextView;
        public TextView dateTextView;

        public ManagerProductViewHolder(View root) {
            super(root);
            imageView = (ImageView) root.findViewById(R.id.image_view);
            titleTextView = (TextView) root.findViewById(R.id.title_text_view);
            descriptionTextView = (TextView) root.findViewById(R.id.description_text_view);
            priceTextView = (TextView) root.findViewById(R.id.price_text_view);
            dateTextView = (TextView) root.findViewById(R.id.date_text_view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ManagerProductListAdapter(Context context, ArrayList<ManagerProduct> dataset) {
        mContext = context;
        mDataset = dataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ManagerProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_manager_product_item, parent, false);

        return new ManagerProductViewHolder(root);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ManagerProductViewHolder holder, int position) {
        final ManagerProduct product = mDataset.get(position);
        try {
            Picasso.get().load(product.img1)
                    .into(holder.imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.titleTextView.setText(product.name);
        holder.descriptionTextView.setText(product.description);
        holder.priceTextView.setText(String.format("$%s", product.price));
        holder.dateTextView.setText(product.date);
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ManagerProductActivity.class);
                intent.putExtra(ManagerProductFragment.PRODUCT_ID_KEY, product.id);
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
