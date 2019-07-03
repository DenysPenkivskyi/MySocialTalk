package com.example.denys.mysocialtalk

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.DateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatActivity"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

        listenForMessages()

        send_button_chat_log.setOnClickListener{
            Log.d(TAG, "Attemp to send message...")
            performSendMessage()
        }
    }

    private  fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val reference = FirebaseDatabase.getInstance().getReference("/user_messages/$fromId/$toId")

        reference.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if(chatMessage != null) {

                    Log.d(TAG, chatMessage.text)
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItemRow(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItemRow(chatMessage.text, toUser!!))
                    }

                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
            }
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun performSendMessage() {

        val text = edittext_chat_log.text.toString()
        val date = DateFormat.getDateTimeInstance().format(System.currentTimeMillis())

        if (text.isEmpty()) {
            Toast.makeText(this, "Message not entered", Toast.LENGTH_SHORT).show()
            return
        }

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromId == null) return

        val fromReference = FirebaseDatabase.getInstance().getReference("/user_messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user_messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(fromReference.key!!, text, fromId!!, toId, date,
                System.currentTimeMillis() /1000 )

        fromReference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "Saved our chat message: ${fromReference.key}")
                    edittext_chat_log.text.clear()
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
                }
        toReference.setValue(chatMessage)

        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("latest_messages/$fromId/$toId")
        latestMessagesRef.setValue(chatMessage)

        val latestMessagesToRef = FirebaseDatabase.getInstance().getReference("latest_messages/$toId/$fromId")
        latestMessagesToRef.setValue(chatMessage)
    }

}



