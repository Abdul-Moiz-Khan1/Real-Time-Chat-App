package moiz.dev.chatapp.Model

data class NotificationModel(
    val username:String,
    val senderId:String,
    val body:String,
    val recieverId:String

){
    constructor() : this(null.toString(), null.toString(), null.toString() , null.toString())
}
