package moiz.dev.chatapp.Model

import android.net.Uri

data class Message(
    var message: String = "",
    val senderId: String = "",
    val timestamp: String = "",
    val messageID:String = "",
    val deleted:Boolean = false,
)
