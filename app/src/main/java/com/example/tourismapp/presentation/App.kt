package com.example.tourismapp.presentation


import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.tourismapp.R
import com.example.tourismapp.presentation.utility.Constants

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

private const val TAG = "APP"

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        //set firebase crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        //create channel for firebase notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.d(TAG, token)
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val myChannel = NotificationChannel(Constants.FIREBASE_SERVICE_CHANNEL_ID, name, importance)
        myChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(myChannel)
    }

}