package moiz.dev.chatapp.Model

import android.net.Uri
import moiz.dev.chatapp.Utils.DeliveryStatus

data class Message(
    var message: String = "",
    val senderId: String = "",
    val timestamp: String = "",
    val messageID: String = "",
    val deleted: Boolean = false,
    val deliveryState: String = DeliveryStatus.sent
)
