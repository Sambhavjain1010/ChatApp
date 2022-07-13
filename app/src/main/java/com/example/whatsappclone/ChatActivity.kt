package com.example.whatsappclone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappclone.adapters.ChatAdapter
import com.example.whatsappclone.modals.*
import com.example.whatsappclone.utils.KeyboardVisibilityUtil
import com.example.whatsappclone.utils.isSameDayAs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.*

const val USER_ID = "userId"
const val USER_THUMB_IMAGE = "thumbImage"
const val USER_NAME = "userName"
class ChatActivity : AppCompatActivity() {

    private val friendId by lazy{
        intent.getStringExtra(USER_ID)
    }
    private val name by lazy{
        intent.getStringExtra(USER_NAME)
    }
    private val image by lazy{
        intent.getStringExtra(USER_THUMB_IMAGE)
    }
    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid
    }
    private val db by lazy{
        FirebaseDatabase.getInstance()
    }

    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil
    lateinit var currentUser: User
    private val messages = mutableListOf<ChatEvent>()
    lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)
        keyboardVisibilityHelper = KeyboardVisibilityUtil(rootView) {
            msgsRv.scrollToPosition(messages.size - 1)
        }
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid!!).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }

        chatAdapter = ChatAdapter(messages,mCurrentUid!!)
        msgsRv.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        nameTv.text = name
        Picasso.get().load(image).into(userImageView)
        listenToMessages{
                msg , update ->
            if (update){
                updateMessage(msg)
            }else{
                addMessage(msg)
            }
        }

        val emojiPopup = EmojiPopup(rootView,msgEdtv)
        smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }

        swipeToLoad.setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                swipeToLoad.isRefreshing = false
            }
        }

        sendBtn.setOnClickListener {
            msgEdtv.text?.let {
                if (it.isNotEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }

        chatAdapter.highFiveClick = {id, status ->
            updateHighFive(id,status)
        }

        updateReadCount()
    }

    private fun updateReadCount() {
        getInbox(mCurrentUid!!,friendId!!).child("count").setValue(0)
    }

    private fun listenToMessages(newMsg: (msg: Message, update: Boolean) -> Unit) {
        getMessages(friendId!!)
            .orderByKey()
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val msg = snapshot.getValue(Message::class.java)
                    newMsg(msg!!, false)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val msg = snapshot.getValue(Message::class.java)!!
                    newMsg(msg, true)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun addMessage(msg: Message) {
        val eventBefore = messages.lastOrNull()
        if ((eventBefore != null && !eventBefore.sentAt.isSameDayAs(msg.sentAt)) || eventBefore == null){
            messages.add(
                DateHeader(
                    msg.sentAt, context = this
                )
            )
        }
        messages.add(msg)
        chatAdapter.notifyItemInserted(messages.size-1)
        msgsRv.scrollToPosition(messages.size-1)
    }
    private fun updateMessage(msg: Message) {
        val position = messages.indexOfFirst {
            when (it) {
                is Message -> it.msgId == msg.msgId
                else -> false
            }
        }
        messages[position] = msg

        chatAdapter.notifyItemChanged(position)
    }
    private fun updateHighFive(id: String, status: Boolean){
        getMessages(friendId!!).child(id).updateChildren(mapOf("liked" to status))
    }

    private fun sendMessage(msg: String) {
        //Unique Key
        val id = getMessages(friendId!!).push().key
        checkNotNull(id){
            "Cannot be Null"
        }

        val msgMap = Message(msg, mCurrentUid!!,id)
        getMessages(friendId!!).child(id).setValue(msgMap).addOnSuccessListener {
        }

        updateLastMessage(msgMap)
    }

    private fun updateLastMessage(message: Message) {
        val inboxMap = Inbox(
            message.msg,
            friendId!!,
            name!!,
            image!!,
            count = 0
        )

        getInbox(mCurrentUid!!, friendId!!).setValue(inboxMap).addOnSuccessListener {
            getInbox(friendId!!, mCurrentUid!!).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)
                    inboxMap.apply {
                        from = message.senderId
                        name = currentUser.name
                        image = currentUser.thumbImage
                        count = 1
                    }

                    value.let {
                        if (it?.from == message.senderId){
                            inboxMap.count = value?.count!! + 1
                        }
                    }

                    getInbox(friendId!!, mCurrentUid!!).setValue(inboxMap)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }
    }

    private fun markAsRead(){
        getInbox(friendId!!, mCurrentUid!!).child("count").setValue(0)
    }

    private fun getMessages(friendId: String) = db.reference.child("messages/${getId(friendId)}")

    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("Chats/$toUser/$fromUser")

    private fun getId(friendId: String): String{
        //Id for the messages
        return if (friendId > mCurrentUid.toString()){
            mCurrentUid + friendId
        }else {
            friendId + mCurrentUid
        }
    }
    override fun onResume() {
        super.onResume()
        rootView.viewTreeObserver
            .addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }

    override fun onPause() {
        super.onPause()
        rootView.viewTreeObserver
            .removeOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }

    companion object {

        fun createChatActivity(context: Context, id: String, name: String, image: String): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(USER_ID, id)
            intent.putExtra(USER_NAME, name)
            intent.putExtra(USER_THUMB_IMAGE, image)

            return intent
        }
    }
}