package uz.pdp.messenger.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import uz.pdp.messenger.MainActivity
import uz.pdp.messenger.R

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val sented = remoteMessage.data["sented"]

        val currentUser = Firebase.auth.currentUser

        if (currentUser != null && sented?.equals(currentUser.uid)!!) {
            val isPersonal = remoteMessage.data["isPersonal"].toBoolean()
            if (isPersonal)
                sendNotificationPersonal(remoteMessage)
            else {
                sendGroupNotification(remoteMessage)
            }
        }


    }

    private fun sendGroupNotification(remoteMessage: RemoteMessage) {
        val group = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val body = remoteMessage.data["body"]
        val groupName = remoteMessage.data["title"]
        val username = remoteMessage.data["username"]

        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putString("gid", group)
        bundle.putString("groupName", groupName)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, "123")
            .setSmallIcon(icon?.toInt()!!)
            .setContentTitle("New message from $groupName")
            .setContentText("$username: $body")
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$username: $body"))
            .setContentIntent(pendingIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        notificationManager.notify(123, builder.build())
    }

    private fun sendNotificationPersonal(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val body = remoteMessage.data["body"]
        val title = remoteMessage.data["title"]
        val username = remoteMessage.data["username"]

        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, "123")
            .setSmallIcon(icon?.toInt()!!)
            .setContentTitle("$title from $username")
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        notificationManager.notify(12, builder.build())
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val currentUser = Firebase.auth.currentUser

        if (currentUser != null) {
            updateToken(p0)
        }
    }

    private fun updateToken(p0: String) {
        val currentUser = Firebase.auth.currentUser

        val database = Firebase.database.reference

        database.child("users/${currentUser?.uid}/token").setValue(p0)
    }

}