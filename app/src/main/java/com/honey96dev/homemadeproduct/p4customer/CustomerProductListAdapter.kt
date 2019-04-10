package com.honey96dev.homemadeproduct.p4customer

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.honey96dev.homemadeproduct.R
import com.squareup.picasso.Picasso

import java.util.ArrayList

class CustomerProductListAdapter// Provide a suitable constructor (depends on the kind of dataset)
(context: Context, private val mDataset: ArrayList<CustomerProduct>) : RecyclerView.Adapter<CustomerProductListAdapter.CustomerProductViewHolder>() {

    // Dataitem
    class CustomerProduct(var id: String, var name: String, var description: String, var icon: String, var likes: String)

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class CustomerProductViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        // each data item is just a string in this case
        var imageView: ImageView
        var titleTextView: TextView
        var descriptionTextView: TextView

        init {
            imageView = root.findViewById<View>(R.id.image_view) as ImageView
            titleTextView = root.findViewById<View>(R.id.title_text_view) as TextView
            descriptionTextView = root.findViewById<View>(R.id.description_text_view) as TextView
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerProductViewHolder {
        // create a new view
        val root = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_customer_product_item, parent, false)

        return CustomerProductViewHolder(root)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CustomerProductViewHolder, position: Int) {
        val product = mDataset[position]
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
        Log.e("icon-url", product.icon)
        Picasso.get().load(product.icon)
                .into(holder.imageView)
        holder.titleTextView.text = product.name
        holder.descriptionTextView.text = product.description
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mDataset.size
    }
}
