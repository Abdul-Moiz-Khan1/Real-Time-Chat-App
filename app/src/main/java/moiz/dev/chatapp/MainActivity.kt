package moiz.dev.chatapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.messaging.FirebaseMessaging
import moiz.dev.chatapp.Adapters.UserAdapter
import moiz.dev.chatapp.Model.Message
import moiz.dev.chatapp.Model.NotificationModel
import moiz.dev.chatapp.Model.User
import moiz.dev.chatapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var usersList: ArrayList<User>
    private lateinit var userAdapter: UserAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val currentUserId = FirebaseAuth.getInstance().uid ?: "123"
    private var recieverId = "temp"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Utils.showToast(this, "Notification permission allowed")
                } else {
                    Utils.showToast(this, "Notification permission denied")
                }
            }
        requestNotificationPermission()

        usersList = ArrayList()
        userAdapter = UserAdapter(this, usersList, currentUserId) { user ->
            val intent = Intent(this, ChatRoom::class.java)
            intent.putExtra("receiverId", user.uid)
            intent.putExtra("username", user.name)
            startActivity(intent)
        }

        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsers.adapter = userAdapter
        databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference.child("users").addValueEventListener(object :
            ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.uid != currentUserId) {
                        usersList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        databaseReference.child("notifications").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (notification in snapshot.children) {
                    Log.d(
                        "customNotifications is viewed value",
                        notification.getValue(NotificationModel::class.java).toString()
                    )
                    val newNotification = notification.getValue(NotificationModel::class.java)!!
                    recieverId = newNotification.recieverId
                    var isViewed = false
                    databaseReference.child("notifications").child(newNotification.senderId)
                        .child("isViewed").get().addOnSuccessListener { snapshot ->
                            isViewed = snapshot.getValue(Boolean::class.java) ?: false
                            Log.d("isViewed", isViewed.toString())
                        }
                    if (newNotification.senderId == currentUserId) {
                        Log.d("isViewedcontinuty", isViewed.toString())
                        continue
                    } else if (isViewed) {
                        Log.d("isViewedINCONDITION", isViewed.toString())
                        continue
                    } else {
                        Log.d("isViewedELSEWISE", isViewed.toString())
                        showNotification(
                            this@MainActivity, newNotification
                        )
                        databaseReference.child("notifications").child(newNotification.senderId)
                            .child("isViewed").setValue(true)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        binding.logoutBtn.setOnClickListener {
            databaseReference.child("users").child(currentUserId).child("isOnline").setValue(false)
            auth.signOut()
            Utils.showToast(this, "User Logged out")
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().uid?.let {
            databaseReference.child("users").child(it).child("isOnline").setValue(true)
        }

    }

    override fun onPause() {
        super.onPause()
        FirebaseAuth.getInstance().uid?.let {
            databaseReference.child("users").child(it).child("isOnline").setValue(false)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun showNotification(context: Context, notification: NotificationModel) {
        Log.d("customNotifications", "in notif funcshhhhh${notification.body}")
        val channelId = "default_channel_id"
        val channelName = "Default Channel"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android 8.0+ (Oreo), create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, ChatRoom::class.java).apply {
            putExtra("receiverId", notification.senderId)
            putExtra("username", notification.username)
            Log.d("recIDfromnotification", recieverId)
            }

        val pendingIntent = TaskStackBuilder.create(context).run {
            // Add the back stack: MainActivity → ChatRoom
            addNextIntentWithParentStack(Intent(context, MainActivity::class.java))
            addNextIntent(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_logo) // Replace with your icon
            .setContentTitle(notification.username) // First string
            .setContentText(notification.body) // Second string
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true).setContentIntent(pendingIntent)

        // Show the notification
        notificationManager.notify(1, builder.build())
    }
}

