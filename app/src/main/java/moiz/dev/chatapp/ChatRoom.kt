package moiz.dev.chatapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import moiz.dev.chatapp.Adapters.MessageAdapter
import moiz.dev.chatapp.Model.Message
import moiz.dev.chatapp.Model.NotificationModel
import moiz.dev.chatapp.Model.User
import moiz.dev.chatapp.databinding.ActivityChatRoomBinding

class ChatRoom : AppCompatActivity() {
    private var messageList = ArrayList<Message>()
    private lateinit var messageAdapter: MessageAdapter


    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var database: DatabaseReference
    private lateinit var senderId: String
    private lateinit var receiverId: String


    private var ownUserName: String = "temp"

    private lateinit var binding: ActivityChatRoomBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        senderId = intent.getStringExtra("receiverId") ?: ""
        if (senderId.isEmpty()) {
            Toast.makeText(this, "Receiver ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        receiverId = intent.getStringExtra("receiverId") ?: return
        val username = intent.getStringExtra("username") ?: return

        senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("recIDthroughitnt", receiverId)
        Log.d("senderrecID", senderId)

        database = FirebaseDatabase.getInstance().reference

        getUserName { name ->
            ownUserName = name.toString()
        }

        database.child("users").child(receiverId).child("isOnline")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isOnline = snapshot.getValue(Boolean::class.java) ?: false
                    Log.d("online", isOnline.toString())
                    if (isOnline) {
                        binding.onlineStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                        binding.onlineStatus.text = "Online"
                    } else {
                        binding.onlineStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        binding.onlineStatus.text = "Offline"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("online", "Failed to read online status", error.toException())
                }
            })
        database.child("users").child(receiverId).child("isTyping")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                    Log.d("typing", isTyping.toString())
                    if (isTyping) {
                        binding.typingStatus.visibility = View.VISIBLE
                    } else {
                        binding.typingStatus.visibility = View.INVISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        binding.chatRoomUserName.text = username

        // ✅ 2. Define chat rooms
        senderRoom = senderId + receiverId
        Log.d("room", senderRoom)
        receiverRoom = receiverId + senderId

        // ✅ 3. Set up RecyclerView and Adapter
        messageList = ArrayList()
        messageAdapter =
            MessageAdapter(this, messageList, senderId, senderRoom, receiverRoom, database)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter


        Log.d("chk reach", "after rec views")
        // ✅ 4. Load messages
        database.child("chats").child(senderRoom).child("messages")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("where", "inChats,senderroom,messages...")
                    messageList.clear()
                    for (msgSnapshot in snapshot.children) {
                        if (msgSnapshot.value is Map<*, *>) {
                            val message = msgSnapshot.getValue(Message::class.java)
                            message?.let {
                                messageList.add(it)
                            }


                        } else {
                            Log.w(
                                "InvalidMessageNode",
                                "Skipped non-message data: ${msgSnapshot.value}"
                            )
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("chk reach", "in on cancel ${error.message}")
                }
            })

        binding.backbutton.setOnClickListener {
            finish()
        }

        // ✅ 5. Send message
        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.messageEditText.addTextChangedListener(object : TextWatcher {
            val looper = Handler(Looper.getMainLooper())
            val stoppedTypingRunable = Runnable { setTypingStatus(false) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setTypingStatus(true)
                looper.removeCallbacks(stoppedTypingRunable)
                looper.postDelayed(stoppedTypingRunable, 1000)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })


    }

    private fun getUserName(callback: (String?) -> Unit) {
        database.child("users").child(senderId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user2 = snapshot.getValue(User::class.java)
                    val name = user2?.name
                    Log.d("username", name.toString())
                    callback(name)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                    callback(null)
                }
            })
    }

    private fun sendNotificationToFirebase(notification: NotificationModel) {
        database.child("notifications").child(senderId).setValue(notification)
            .addOnSuccessListener {
                Utils.showToast(this, "Notification pushed to firebase")
            }.addOnFailureListener { e ->
                Utils.showToast(this, "Unable to push to firebase")
                Log.d("customNotificationsWHILESENDINGNOTIF", e.message.toString())
            }
    }

    private fun sendMessage() {
        Log.d("chk reach", "in send button")
        Toast.makeText(this, "in send button", Toast.LENGTH_SHORT).show()
        val messageText = binding.messageEditText.text.toString().trim()
        val messageKey =
            database.child("chats").child(senderRoom).child("messages").push().key!!

        if (messageText.isNotEmpty()) {
            val message =
                Message(
                    messageText,
                    senderId,
                    Utils.convertToTimestamp(System.currentTimeMillis()),
                    messageID = messageKey,
                )
            database.child("chats").child(senderRoom).child("messages").child(messageKey)
                .setValue(message)
                .addOnSuccessListener {
                    Log.d("msgkey", messageKey)
                    database.child("chats").child(receiverRoom).child("messages")
                        .child(messageKey)
                        .setValue(message)
                }
            val newNotification =
                NotificationModel(ownUserName, senderId, message.message, senderId)
            Log.d("customNotificationsBeforeSenidng", newNotification.toString())
            Log.d("notificaionwaliid", senderId)
            sendNotificationToFirebase(newNotification)
            binding.messageEditText.setText("")
        }
    }

    private fun setTypingStatus(state: Boolean) {
        senderId.let {
            database.child("users").child(it).child("isTyping").setValue(state)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::senderId.isInitialized) {
            database.child("users").child(senderId).child("isOnline").setValue(true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::senderId.isInitialized) {
            database.child("users").child(senderId).child("isOnline").setValue(false)
            database.child("users").child(senderId).child("lastSeen")
                .setValue(Utils.convertToTimestamp(System.currentTimeMillis()))
        }
    }
}