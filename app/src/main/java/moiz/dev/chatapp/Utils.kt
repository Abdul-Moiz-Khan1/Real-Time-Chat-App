package moiz.dev.chatapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import moiz.dev.chatapp.Model.NotificationModel
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