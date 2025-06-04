package moiz.dev.chatapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import moiz.dev.chatapp.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val fadeIn = ObjectAnimator.ofFloat(binding.splashText, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(binding.splashText, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.splashText, "scaleY", 0.8f, 1f)

        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            duration = 3000
            start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }, 3000)

    }
}