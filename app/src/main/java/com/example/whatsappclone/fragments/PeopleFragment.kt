package com.example.whatsappclone.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.*
import com.example.whatsappclone.adapters.UserViewHolder
import com.example.whatsappclone.modals.User
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragments_chats.*

private const val DELETED_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2
class PeopleFragment : Fragment() {

    lateinit var mAdapter : FirestorePagingAdapter<User,RecyclerView.ViewHolder>
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        //Make sure you are using the same key
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name",Query.Direction.ASCENDING)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate the layout for this fragment
        setUpAdapter()
        return inflater.inflate(R.layout.fragments_chats,container,false)
    }

    private fun setUpAdapter() {
        //Init paging configuration
        val config = PagingConfig(
            10,
            2,
            false,
        )

        //Init Adapter configuration
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(database,config,User::class.java)
            .build()

        //Instantiate Paging Adapter
        mAdapter = object : FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return when(viewType){
                    DELETED_VIEW_TYPE -> UserViewHolder(layoutInflater.inflate(R.layout.list_item, parent, false))
                    else -> EmptyViewHolder(layoutInflater.inflate(R.layout.empty_view, parent, false))
                }
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                if (holder is UserViewHolder) {
                    holder.bind(user = model){ name: String, photo: String, uid: String ->
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra(USER_NAME,name)
                        intent.putExtra(USER_THUMB_IMAGE,photo)
                        intent.putExtra(USER_ID,uid)
                        startActivity(intent)
                    }
                }else {
                    //Todo - Something
                }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)

                return if (auth.uid == item!!.uid){
                    DELETED_VIEW_TYPE
                }else{
                    NORMAL_VIEW_TYPE
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }
}
/**
 * Suppose you have 1000 users
 * 1 user = 10Kb
 * 10 * 1000 = 10000Kb - You may get a Timeout
 * Pagination - getting data in pages
 */