package moiz.dev.chatapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import moiz.dev.chatapp.databinding.ActivitySignInBinding

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, SignIn::class.java))
            finish()
        },2000)

    }
}