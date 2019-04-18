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

class CustomerCartListAdapter// Provide a suitable constructor (depends on the kind of dataset)
(internal var mContext: Context, internal var mDataset: ArrayList<CustomerCartItem>) : RecyclerView.Adapter<CustomerCartListAdapter.CustomerCartItemViewHolder>() {

    // Dataitem
    class CustomerCartItem(var id: String, storeID: String, var name: String, var img1: String, var uprice: Double,
                           var quantity: Double) {
        var storeID: String
        var price: Double
        init {
            this.storeID = storeID
            this.price = uprice * quantity
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class CustomerCartItemViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        // each data item is just a string in this case
        var imageView: ImageView
        var titleTextView: TextView
        var upriceTextView: TextView
        var quantityTextView: TextView
        var priceTextView: TextView

        init {
            imageView = root.findViewById<View>(R.id.image_view) as ImageView
            titleTextView = root.findViewById<View>(R.id.title_text_view) as TextView
            upriceTextView = root.findViewById<View>(R.id.uprice_text_view) as TextView
            quantityTextView = root.findViewById<View>(R.id.quantity_text_view) as TextView
            priceTextView = root.findViewById<View>(R.id.price_text_view) as TextView
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerCartItemViewHolder {
        // create a new view
        val root = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_customer_cart_item, parent, false)

        return CustomerCartItemViewHolder(root)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CustomerCartItemViewHolder, position: Int) {
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

//        Double uprice = Dou
        holder.titleTextView.text = product.name
        holder.upriceTextView.text = String.format("Unit price: $%s", product.uprice)
        holder.quantityTextView.text = String.format("Quantity: %s", product.quantity)
        holder.priceTextView.text = String.format("Price: $%s", product.price)

//        holder.itemView.setOnClickListener {
//            val intent = Intent(mContext, CustomerProductActivity::class.java)
//            intent.putExtra(ManagerProductFragment.PRODUCT_ID_KEY, product.id)
//            mContext.startActivity(intent)
//        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mDataset.size
    }
}
