package com.honey96dev.homemadeproduct.p4customer;

import android.content.Context;
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

public class CustomerProductListAdapter extends RecyclerView.Adapter<CustomerProductListAdapter.CustomerProductViewHolder> {
    private ArrayList<CustomerProduct> mDataset;

    // Dataitem
    public static class CustomerProduct {
        public String id;
        public String name;
        public String description;
        public String icon;
        public String likes;
        public CustomerProduct(String id, String name, String desctiption, String icon, String likes) {
            this.id = id;
            this.name = name;
            this.description = desctiption;
            this.icon = icon;
            this.likes = likes;
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class CustomerProductViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView imageView;
        public TextView titleTextView;
        public TextView descriptionTextView;

        public CustomerProductViewHolder(View root) {
            super(root);
            imageView = (ImageView) root.findViewById(R.id.image_view);
            titleTextView = (TextView) root.findViewById(R.id.title_text_view);
            descriptionTextView = (TextView) root.findViewById(R.id.description_text_view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CustomerProductListAdapter(Context context, ArrayList<CustomerProduct> dataset) {
        mDataset = dataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CustomerProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_customer_product_item, parent, false);

        return new CustomerProductViewHolder(root);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CustomerProductViewHolder holder, int position) {
        CustomerProduct product = mDataset.get(position);
//        try {
//            Log.e("icon-url", product.icon);
//            URL imageUrl = new URL(product.icon);
//            Bitmap icon = BitmapFactory.decodeStream(imageUrl.openStream());
//            holder.imageView.setImageBitmap(icon);
////            holder.imageView.setImageBitmap(BitmapFactory.decodeStream(imageUrl.openConnection() .getInputStream()));
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        holder.imageView.setImageURI(Uri.parse(product.icon));
        Log.e("icon-url", product.icon);
        Picasso.get().load(product.icon)
                .into(holder.imageView);
        holder.titleTextView.setText(product.name);
        holder.descriptionTextView.setText(product.description);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
