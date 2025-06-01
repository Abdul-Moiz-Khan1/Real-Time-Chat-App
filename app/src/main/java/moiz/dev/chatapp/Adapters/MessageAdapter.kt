package moiz.dev.chatapp.Adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import moiz.dev.chatapp.Model.Message
import moiz.dev.chatapp.R

class MessageAdapter(
    private val context: Context,
    private val messageList: ArrayList<Message>,
    private val currentUserId: String
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

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val message = messageList[position]
        if (holder is SentViewHolder) {
            holder.sentText.text = message.message
            holder.sentTextTime.text = message.timestamp
            if (message.imageUri.toString().isEmpty()) holder.imageview.visibility = View.GONE
            else {
                holder.imageview.visibility = View.VISIBLE
                val imageUriString = message.imageUri
                if (!imageUriString.isNullOrEmpty()) {
                    val imageUri = Uri.parse(imageUriString)
                    Glide.with(holder.itemView.context)
                        .load(imageUri)       // optional
                        .into(holder.imageview)
                } else {
                    holder.imageview.visibility = View.GONE
                    // or hide the ImageView
                }
            }
            if (message.videoUri.toString().isEmpty()) holder.videoview.visibility = View.GONE
            else {
//                holder.videoview.visibility = View.VISIBLE
                //playpause video
            }
        } else if (holder is ReceivedViewHolder) {
            holder.receivedText.text = message.message
            holder.receivedTextTime.text = message.timestamp
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
        val imageview = view.findViewById<ImageView>(R.id.sentImage)
        val videoview = view.findViewById<VideoView>(R.id.sentVideo)
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receivedText: TextView = view.findViewById(R.id.receivedMessageText)
        val receivedTextTime: TextView = view.findViewById(R.id.recievedmsgTime)
    }
}