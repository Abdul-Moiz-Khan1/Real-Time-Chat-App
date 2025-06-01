package moiz.dev.chatapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import moiz.dev.chatapp.databinding.ActivityChatRoomBinding

class ChatRoom : AppCompatActivity() {
    private var messageList = ArrayList<Message>()
    private lateinit var messageAdapter: MessageAdapter


    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var database: DatabaseReference
    private lateinit var senderId: String
    private lateinit var receiverId: String

    private val IMAGE_PICK_CODE = 1001
    private val VIDEO_PICK_CODE = 1002
    private val DOC_PICK_CODE = 1003

    private var imageUri: Uri? = null
    private var videoUri: Uri? = null
    private var documentUri: Uri? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        receiverId = intent.getStringExtra("receiverId") ?: return
        val username = intent.getStringExtra("username") ?: return

        database = FirebaseDatabase.getInstance().reference

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



        binding.chatRoomUserName.text = username


        // ✅ 2. Define chat rooms
        senderRoom = senderId + receiverId
        receiverRoom = receiverId + senderId

        // ✅ 3. Set up RecyclerView and Adapter
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, senderId)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter


        Log.d("chk reach", "after rec views")
        // ✅ 4. Load messages
        database.child("chats").child(senderRoom).child("messages")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (msgSnapshot in snapshot.children) {
                        val message = msgSnapshot.getValue(Message::class.java)
                        message?.let { messageList.add(it) }
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
            Log.d("chk reach", "in send button")
            Toast.makeText(this, "in send button", Toast.LENGTH_SHORT).show()
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message =
                    Message(
                        messageText,
                        senderId,
                        Utils.convertToTimestamp(System.currentTimeMillis()),
                        imageUri.toString(),
                        videoUri.toString(),
                        documentUri.toString()
                    )
                database.child("chats").child(senderRoom).child("messages")
                    .push().setValue(message)
                    .addOnSuccessListener {
                        database.child("chats").child(receiverRoom).child("messages")
                            .push().setValue(message)
                    }
                binding.messageEditText.setText("")
            }
        }

        binding.selectFiles.setOnClickListener {
            requestPermissionsofMedia(this)
            binding.filesSelector.visibility = View.VISIBLE
        }
        binding.chatRecyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && binding.filesSelector.visibility == View.VISIBLE) {
                binding.filesSelector.visibility = View.GONE
            }
            false
        }

        binding.selectImages.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
        binding.selectVideos.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, VIDEO_PICK_CODE)
        }
        binding.selectDocuments.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            val mimeTypes = arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
                "text/plain"
            )
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(intent, DOC_PICK_CODE)
        }

    }

    override fun onResume() {
        super.onResume()
        senderId.let {
            database.child("users").child(it).child("isOnline").setValue(true)
        }
    }

    override fun onPause() {
        super.onPause()
        senderId.let {
            database.child("users").child(it).child("isOnline").setValue(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val fileUri = data.data
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    imageUri = fileUri
                }

                VIDEO_PICK_CODE -> {
                    videoUri = fileUri
                }

                DOC_PICK_CODE -> {
                    documentUri = fileUri
                }
            }
        }
    }


}

private fun requestPermissionsofMedia(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        }
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(android.Manifest.permission.READ_MEDIA_VIDEO)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                context as Activity,
                permissions.toTypedArray(),
                101
            )
        }
    }

}
