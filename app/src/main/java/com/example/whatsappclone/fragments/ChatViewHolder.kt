package com.example.whatsappclone.fragments

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.modals.Inbox
import com.example.whatsappclone.utils.formatAsListItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.view.*
import kotlinx.android.synthetic.main.list_item.view.*

class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(item: Inbox, onClick: (name: String, photo: String, id: String) -> Unit) =
        with(itemView){
            countTv.isVisible = item.count > 0
            countTv.text = item.count.toString()
            timeTv.text = item.time.formatAsListItem(context)

            titleTv.text = item.name
            statusTv.text = item.msg

            Picasso.get()
                .load(item.image)
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .error(R.drawable.ic_baseline_account_circle_24)
                .into(userImageView)

            setOnClickListener{
                onClick.invoke(item.name, item.image, item.from)
            }
        }
}