package com.example.whatsappclone.adapters

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.modals.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item.view.*

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(user : User, onClick:(name:String,photo:String,id:String) -> Unit) = with(itemView){
        countTv.isVisible = false
        timeTv.isVisible = false

        titleTv.text = user.name
        statusTv.text = user.status
        Picasso.get()
            .load(user.thumbImage)
            .placeholder(R.drawable.ic_baseline_account_circle_24)
            .error(R.drawable.ic_baseline_account_circle_24)
            .into(thumbImageView)

        setOnClickListener{
            onClick.invoke(user.name,user.thumbImage,user.uid)
        }
    }
}