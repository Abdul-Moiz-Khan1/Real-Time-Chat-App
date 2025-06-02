package moiz.dev.chatapp.Adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import moiz.dev.chatapp.Model.User
import moiz.dev.chatapp.R

class UserAdapter(
    private val context: Context,
    private val userList: ArrayList<User>,
    private val currentUserId: String,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = itemView.findViewById(R.id.userNameText)
        val wholeView = itemView.findViewById<LinearLayout>(R.id.user_item_layout)
        val lastSeen = itemView.findViewById<TextView>(R.id.lastSeen)
        fun bind(user: User) {
            nameText.text = user.name
            lastSeen.text = user.lastSeen
            wholeView.setOnClickListener { onUserClick(user) }
        }
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}