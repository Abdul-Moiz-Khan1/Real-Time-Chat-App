package moiz.dev.chatapp.Model

data class NotificationModel(
    val username:String,
    val senderId:String,
    val body:String,
    val recieverId:String,
    val isViewed: Boolean

){
    constructor() : this(null.toString(), null.toString(), null.toString() , null.toString() , false)
}
