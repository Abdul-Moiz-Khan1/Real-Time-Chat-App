package moiz.dev.chatapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import moiz.dev.chatapp.databinding.ActivityNewUserSignInBinding

class NewUserSignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityNewUserSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.newUserLoginbutton.setOnClickListener {
            if ( binding.newUserPassword.text?.toString()?.length!! >= 6 && binding.confirmPassword.text.toString() == binding.newUserPassword.text.toString()
            ) {
                AddNewUser(
                    binding.newUserEmail2.text.toString(),
                    binding.newUserPassword.text.toString()
                )
            } else {
                Utils.showToast(this, "Invalid Email or Password")
            }
        }

        binding.gotoLogIn.setOnClickListener {
            finish()
        }


    }

    private fun AddNewUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Utils.showToast(this, "User Added Successfully")
                FirebaseAuth.getInstance().signOut()

                finish()
            } else {
                Utils.showToast(this, "User Not Added")
            }


        }.addOnFailureListener { e ->
            Utils.showToast(this, "User Login Failed${e.message.toString()}")
        }
    }
}


