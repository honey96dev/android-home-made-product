package com.honey96dev.homemadeproduct.p4manager

import android.content.Context
import android.content.Intent
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

class ManagerStoreListAdapter// Provide a suitable constructor (depends on the kind of dataset)
(internal var mContext: Context, internal var mDataset: ArrayList<ManagerStore>) : RecyclerView.Adapter<ManagerStoreListAdapter.ManagerStoreViewHolder>() {

    // Dataitem
    class ManagerStore(var id: String, var name: String, var description: String, var icon: String, var likes: String)

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ManagerStoreViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        // each data item is just a string in this case
        var imageView: ImageView
        var titleTextView: TextView
        var descriptionTextView: TextView
        var likesTextView: TextView

        init {
            imageView = root.findViewById<View>(R.id.image_view) as ImageView
            titleTextView = root.findViewById<View>(R.id.title_text_view) as TextView
            descriptionTextView = root.findViewById<View>(R.id.description_text_view) as TextView
            likesTextView = root.findViewById<View>(R.id.likesTextView) as TextView
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagerStoreViewHolder {
        // create a new view
        val root = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_manager_store_item, parent, false)

        return ManagerStoreViewHolder(root)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ManagerStoreViewHolder, position: Int) {
        val store = mDataset[position]
        try {
            Picasso.get().load(store.icon)
                    .into(holder.imageView)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.titleTextView.text = store.name
        holder.descriptionTextView.text = store.description
        holder.likesTextView.text = store.likes

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, ManagerProductListActivity::class.java)
            intent.putExtra(ManagerProductListFragment.STORE_ID_KEY, store.id)
            mContext.startActivity(intent)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mDataset.size
    }
}
