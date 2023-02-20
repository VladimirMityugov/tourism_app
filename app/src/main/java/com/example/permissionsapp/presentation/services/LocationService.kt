package com.example.permissionsapp.presentation.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.ViewModelProvider
import com.example.permissionsapp.presentation.MyViewModel
import com.example.permissionsapp.presentation.utility.Constants
import com.example.permissionsapp.presentation.utility.Constants.ACTION_PAUSE
import com.example.permissionsapp.presentation.utility.Constants.ACTION_SHOW_MAPS_FRAGMENT
import com.example.permissionsapp.presentation.utility.Constants.ACTION_START
import com.example.permissionsapp.presentation.utility.Constants.ACTION_STOP
import com.example.permissionsapp.presentation.utility.Constants.INTERVAL_FOR_LOCATION_UPDATES
import com.example.permissionsapp.presentation.utility.Constants.LOCATION_NOTIFICATION_ID
import com.example.permissionsapp.presentation.utility.Constants.LOCATION_SERVICE_CHANNEL_ID
import com.example.permissionsapp.presentation.utility.DefaultLocationClient
import com.example.permissionsapp.presentation.utility.LocationClient
import com.example.permissionsapp.ui.main.MainActivity
import com.example.tourismApp.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "LOCATION_SERVICE"

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private var isFirstLaunch = true
//    val viewModel = ViewModelProvider(this)[MyViewModel::class.java]

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        locationClient = DefaultLocationClient(
            this,
            LocationServices.getFusedLocationProviderClient(this)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (isFirstLaunch) {
                    start()
                    isFirstLaunch = false
                } else {
                    Log.d(TAG, "Resuming service")
                }
            }
            ACTION_PAUSE -> {
                Log.d(TAG, "Service is paused")
            }
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {

        //Create channel
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }


        //Create notification
        val notification = NotificationCompat.Builder(this, LOCATION_SERVICE_CHANNEL_ID)
            .setContentTitle("My location")
            .setContentText("Location: null")
            .setSmallIcon(
                IconCompat.createWithResource(this, R.drawable.new_route_icon)
            )
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(getMainActivityPendingIntent())

        //Subscribe to location updates
        locationClient.getLocationUpdates(INTERVAL_FOR_LOCATION_UPDATES)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                Log.d(TAG, "Location is ${location.latitude}, ${location.longitude}")
                val latitude = location.latitude
                val longitude = location.longitude
                val updatedNotification =
                    notification.setContentText("Location: Lat = $latitude, lon = $longitude")
                notificationManager.notify(LOCATION_NOTIFICATION_ID, updatedNotification.build())
            }
            .launchIn(serviceScope)

        //Start service with notifications
        startForeground(LOCATION_NOTIFICATION_ID, notification.build())
    }

    private fun stop() {
        //Stop service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_MAPS_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val name = Constants.LOCATION_CHANNEL_NAME
        val importance = NotificationManager.IMPORTANCE_LOW
        val locationChannel =
            NotificationChannel(LOCATION_SERVICE_CHANNEL_ID, name, importance)
        notificationManager.createNotificationChannel(locationChannel)
    }

    override fun onDestroy() {
        //Cancel coroutine scope
        super.onDestroy()
        serviceScope.cancel()
    }

}