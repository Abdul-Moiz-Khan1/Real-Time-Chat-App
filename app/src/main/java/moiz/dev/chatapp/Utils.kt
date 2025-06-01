package moiz.dev.chatapp

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {
    fun showToast(context: Context, msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    fun convertToTimestamp(lng: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val time = sdf.format(lng)
        return time
    }

}