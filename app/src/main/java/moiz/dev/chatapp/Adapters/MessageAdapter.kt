package moiz.dev.chatapp.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import moiz.dev.chatapp.Model.Message
import moiz.dev.chatapp.R
import moiz.dev.chatapp.Utils.DeliveryStatus
import moiz.dev.chatapp.Utils.Utils

class MessageAdapter(
    private val context: Context,
    private val messageList: ArrayList<Message>,
    private val currentUserId: String,
    private val senderRoom: String,
    private val recieverRoom: String,
    val database: DatabaseReference
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM_SENT = 1
    private val ITEM_RECEIVED = 2

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_recieved, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (holder is SentViewHolder) {
            holder.sentText.text =
                if (message.deleted) "This message is deleted" else message.message
            holder.sentTextTime.text = message.timestamp

            Log.d("doubleTickChk", "${message.deliveryState} , message = ${message.message}")
            if (message.deliveryState == DeliveryStatus.sent) {
                holder.readReceipt.setImageResource(R.drawable.single_tick)
                Log.d("doubleTickChk", "false")
            } else if (message.deliveryState == DeliveryStatus.delivered) {
                holder.readReceipt.setImageResource(R.drawable.double_tick)
                Log.d("doubleTickChk", "true")
            }

            holder.sentMsgViw.setOnLongClickListener {
                val popup = PopupMenu(context, it)
                popup.menuInflater.inflate(R.menu.message_options_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.deleteForMe -> {
                            deleteForMe(message)
                            popup.dismiss()
                        }

                        R.id.deleteForEveryone -> {
                            deleteForAll(message)
                            popup.dismiss()
                        }
                    }
                    true
                }
                popup.show()
                true
            }

        } else if (holder is ReceivedViewHolder) {
            holder.receivedText.text =
                if (message.deleted) "This message is deleted" else message.message
            holder.receivedTextTime.text = message.timestamp

            if (message.deliveryState != DeliveryStatus.delivered) {
                FirebaseDatabase.getInstance().reference
                    .child("chats").child(senderRoom).child("messages")
                    .child(message.messageID).child("deliveryState")
                    .setValue(DeliveryStatus.delivered)
                FirebaseDatabase.getInstance().reference
                    .child("chats").child(recieverRoom).child("messages")
                    .child(message.messageID).child("deliveryState")
                    .setValue(DeliveryStatus.delivered)
            }
        }
    }


    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId == currentUserId) ITEM_SENT else ITEM_RECEIVED
    }


    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sentText: TextView = view.findViewById(R.id.sentMessageText)
        val sentTextTime: TextView = view.findViewById(R.id.msgtime)
        val sentMsgViw: CardView = view.findViewById(R.id.sentMessageView)
        val readReceipt: ImageView = view.findViewById(R.id.deliveryImage)
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receivedText: TextView = view.findViewById(R.id.receivedMessageText)
        val receivedTextTime: TextView = view.findViewById(R.id.recievedmsgTime)
    }

    fun deleteForMe(message: Message) {
        database.child("chats").child(senderRoom).child("messages").child(message.messageID)
            .setValue(message.apply { this.message = "Deleted for you" })
            .addOnSuccessListener {
                Utils.showToast(context, "Message Deleted for you")
            }
    }

    fun deleteForAll(message: Message) {
        database.child("chats").child(senderRoom).child("messages").child(message.messageID)
            .setValue(message.apply { this.message = "Deleted for Everyone" })
            .addOnSuccessListener {
                Utils.showToast(context, "Message Deleted")
            }
        database.child("chats").child(recieverRoom).child("messages").child(message.messageID)
            .setValue(message.apply { this.message = "Deleted for Everyone" })
            .addOnSuccessListener {
                Utils.showToast(context, "Message Deleted")
            }
    }
}




