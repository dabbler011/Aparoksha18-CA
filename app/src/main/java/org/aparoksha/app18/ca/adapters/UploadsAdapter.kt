package org.aparoksha.app18.ca.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.Query
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.uploads_container.view.*
import org.aparoksha.app18.ca.R
import org.aparoksha.app18.ca.models.Image
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.bumptech.glide.Glide

/**
 * Created by akshat on 12/12/17.
 */

class UploadsAdapter : FirebaseRecyclerAdapter<Image, UploadsAdapter.UploadsViewHolder> {

    var mStorageReference : StorageReference
    var mContext: Context

    constructor(mRef: Query, mStorageReference: StorageReference, mContext : Context) : super(
            Image::class.java,
            R.layout.uploads_container,
            UploadsViewHolder::class.java,
            mRef) {
        this.mStorageReference = mStorageReference
        this.mContext = mContext
    }

    override fun populateViewHolder(viewHolder: UploadsViewHolder?, model: Image?, position: Int) {
        if (model != null) {
            viewHolder?.bindView(model,position,mStorageReference,mContext)
        }
    }


    class UploadsViewHolder(private var mView: View) : RecyclerView.ViewHolder(mView) {

        fun bindView(image: Image, position: Int, mStorageReference: StorageReference, mContext: Context) {
            Glide.with(mContext)
                    .using(FirebaseImageLoader())
                    .load(mStorageReference.child(image.path))
                    .into(mView.uploadID)
            mView.index.text = (position+1).toString()+"."
            if(image.verified) {
                Glide.with(mContext)
                        .load(R.drawable.tick)
                        .into(mView.verifiedID)
            } else {
                Glide.with(mContext)
                        .load(R.drawable.cross)
                        .into(mView.verifiedID)
            }
        }

    }
}