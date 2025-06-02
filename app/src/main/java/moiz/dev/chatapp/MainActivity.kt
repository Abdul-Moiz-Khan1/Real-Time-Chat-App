package moiz.dev.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import moiz.dev.chatapp.Adapters.UserAdapter
import moiz.dev.chatapp.Model.User
import moiz.dev.chatapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var usersList: ArrayList<User>
    private lateinit var userAdapter: UserAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val currentUserId = FirebaseAuth.getInstance().uid ?: "123"
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
}

