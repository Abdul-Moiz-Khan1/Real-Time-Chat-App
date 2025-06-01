package moiz.dev.chatapp.Model

import android.net.Uri

data class Message(
    val message: String = "",
    val senderId: String = "",
    val timestamp: String = "",
    val imageUri: String = "",
    val videoUri: String = "",
    val docuementUri: String = "",
)
