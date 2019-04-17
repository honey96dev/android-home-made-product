package com.honey96dev.homemadeproduct.p4manager

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.honey96dev.homemadeproduct.R
import com.honey96dev.homemadeproduct.p4customer.CustomerProductActivity
import com.squareup.picasso.Picasso
import java.util.*

class CustomerProductListAdapter// Provide a suitable constructor (depends on the kind of dataset)
(internal var mContext: Context, internal var mDataset: ArrayList<CustomerProduct>) : RecyclerView.Adapter<CustomerProductListAdapter.CustomerProductViewHolder>() {

    // Dataitem
    class CustomerProduct(var id: String, storeID: String, var name: String, var description: String,
                          var img1: String, var img2: String, var img3: String, var img4: String, var img5: String, var img6: String,
                          var price: String, var date: String) {
        var storeID: String

        init {
            this.storeID = storeID
            this.storeID = storeID
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class CustomerProductViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        // each data item is just a string in this case
        var imageView: ImageView
        var titleTextView: TextView
        var descriptionTextView: TextView
        var priceTextView: TextView
        var dateTextView: TextView

        init {
            imageView = root.findViewById<View>(R.id.image_view) as ImageView
            titleTextView = root.findViewById<View>(R.id.title_text_view) as TextView
            descriptionTextView = root.findViewById<View>(R.id.description_text_view) as TextView
            priceTextView = root.findViewById<View>(R.id.price_text_view) as TextView
            dateTextView = root.findViewById<View>(R.id.date_text_view) as TextView
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
        try {
            Picasso.get().load(product.img1)
                    .into(holder.imageView)
            if (!product.img1.isEmpty()) {
                holder.imageView.background = null
            }
        } catch (e: Exception) {
            holder.imageView.background = mContext.getDrawable(R.drawable.image_border)
            e.printStackTrace()
        }

        holder.titleTextView.text = product.name
        holder.descriptionTextView.text = product.description
        holder.priceTextView.text = String.format("$%s", product.price)
        holder.dateTextView.text = product.date

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, CustomerProductActivity::class.java)
            intent.putExtra(ManagerProductFragment.PRODUCT_ID_KEY, product.id)
            mContext.startActivity(intent)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mDataset.size
    }
}
