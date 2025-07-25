package moiz.dev.chatapp

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.MainScope
import moiz.dev.chatapp.Model.User
import moiz.dev.chatapp.Utils.Utils
import moiz.dev.chatapp.databinding.ActivitySignInBinding

class SignIn : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference.child("users")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)



        binding.SignInbutton.setOnClickListener {
            if (binding.email.text.isNullOrEmpty() || binding.editTextTextPassword.text.isNullOrEmpty()) {
                Utils.showToast(this, "Please enter email and password")
            } else {
                signinWithEmailPassword(
                    binding.email.text.toString(),
                    binding.editTextTextPassword.text.toString()
                )

            }
        }

        binding.googleSignInBtn.setOnClickListener {
            signIn()
        }

        binding.gotoSignUp.setOnClickListener {
            startActivity(Intent(this, NewUserSignIn::class.java))
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userData = User(
                    user!!.uid,
                    user.displayName!!,
                    user.email!!,
                    Utils.convertToTimestamp(System.currentTimeMillis()),
                    hasUnreadMessage = false
                )
                dbRef.child(user.uid).setValue(userData)
                Toast.makeText(this, "Welcome User", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Firebase authentication failed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun signinWithEmailPassword(email: String, pass: String) {
        auth.signInWithEmailAndPassword(
            email, pass
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Utils.showToast(this, "Login Successful")
                val user = auth.currentUser
                val userData = User(
                    user!!.uid,
                    user.displayName?:user.email.toString().dropLast(10),
                    user.email!!,
                    Utils.convertToTimestamp(System.currentTimeMillis())
                )
                dbRef.child(user.uid).setValue(userData)
                Utils.showToast(this, "Welcome User")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }.addOnFailureListener { e ->
            Utils.showToast(this, "Login Failed${e.message.toString()}")
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}



