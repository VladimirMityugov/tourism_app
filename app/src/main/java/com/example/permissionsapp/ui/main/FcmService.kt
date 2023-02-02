package com.example.permissionsapp.ui.main


import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.permissionsapp.presentation.App
import com.example.tourismApp.R
import com.google.android.datatransport.BuildConfig

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.*

class FcmService: FirebaseMessagingService() {

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
            putExtra(Settings.EXTRA_CHANNEL_ID, App.CHANNEL_ID)
        }

        val pendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.getActivity(
                this,
                1,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            else PendingIntent.getActivity(
                this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val largeIcon = createBitmap(this, R.drawable.photographer)
            val notification = NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setSmallIcon(
                    IconCompat.createWithResource(this, R.drawable.ic_notification_icon)
                )
                .setContentTitle(if(message.data.isNotEmpty()) {message.data["nickname"]}
                else message.notification?.title
                )
                .setContentText(if(message.data.isNotEmpty()){convertDate(message.data["date"]) + ": " + message.data["message"]}
                else message.notification?.body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setColor(Color.argb(255,241,106,42))
                .setAutoCancel(true)
                .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)

    }

    private fun createBitmap(context: Context, vectorResId: Int): Bitmap? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            return bitmap
        }
    }

    private fun convertDate(date: String?): String {
        date ?: return ""
        return SimpleDateFormat(
            "dd-MM-yyyy hh-mm",
            Locale.getDefault()
        ).format(date.toLong())
    }

    companion object {
        private const val NOTIFICATION_ID = 2
    }
}