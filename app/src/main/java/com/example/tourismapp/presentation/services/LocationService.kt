package com.example.tourismapp.presentation.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.tourismapp.R
import com.example.tourismapp.presentation.utility.Constants
import com.example.tourismapp.presentation.utility.Constants.ACTION_PAUSE
import com.example.tourismapp.presentation.utility.Constants.ACTION_SHOW_MAPS_FRAGMENT
import com.example.tourismapp.presentation.utility.Constants.ACTION_START
import com.example.tourismapp.presentation.utility.Constants.ACTION_STOP
import com.example.tourismapp.presentation.utility.Constants.INTERVAL_FOR_LOCATION_UPDATES
import com.example.tourismapp.presentation.utility.Constants.LOCATION_NOTIFICATION_ID
import com.example.tourismapp.presentation.utility.Constants.LOCATION_SERVICE_CHANNEL_ID
import com.example.tourismapp.presentation.utility.location.DefaultLocationClient
import com.example.tourismapp.presentation.utility.location.LocationClient
import com.example.tourismapp.presentation.utility.permissions.hasLocationPermission
import com.example.tourismapp.presentation.utility.permissions.hasNotificationPermission
import com.example.tourismapp.presentation.utility.permissions.hasServicePermission
import com.example.tourismapp.ui.main.MainActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private var isFirstLaunch = true
    private var startTime = 0L
    private var totalTime = 0L
    private var lapTime = 0L


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
                    resume()
                }
            }
            ACTION_PAUSE -> {
                pause()
            }
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pause() {
        is_tracking.value = false
    }

    private fun resume() {
        addEmptyPolyline()
        launchRouteTimer()
        is_tracking.value = true
    }

    private fun launchRouteTimer() {
        startTime = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Default).launch {
            while (is_tracking.value) {
                lapTime = System.currentTimeMillis() - startTime
            }
            totalTime += lapTime
        }
    }

    private fun start() {
        addEmptyPolyline()
        is_tracking.value = true
        is_on_route.value = true
        launchRouteTimer()
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
        if(applicationContext.hasLocationPermission()
            && applicationContext.hasNotificationPermission()
            && applicationContext.hasServicePermission()){
            locationClient.getLocationUpdates(INTERVAL_FOR_LOCATION_UPDATES)
                .catch { e -> e.printStackTrace() }
                .onEach { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val updatedNotification =
                        notification.setContentText("Location: Lat = $latitude, lon = $longitude")
                    notificationManager.notify(LOCATION_NOTIFICATION_ID, updatedNotification.build())
                    if (is_tracking.value) {
                        addPathPoint(location)
                    }
                }
                .launchIn(serviceScope)
        }

        //Start service with notifications
        startForeground(LOCATION_NOTIFICATION_ID, notification.build())
    }

    private fun stop() {
        //Stop service
        is_tracking.value = false
        is_on_route.value = false
        total_time.value = totalTime
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun addEmptyPolyline() = route_path.value.add(mutableListOf())
    private fun addPathPoint(location: Location) {
        val pos = LatLng(location.latitude, location.longitude)
        if(!route_path.value.last().contains(pos)){
            route_path.value.last().add(pos)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_MAPS_FRAGMENT
        },
        FLAG_IMMUTABLE
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

    companion object {

        private var is_tracking = MutableStateFlow(false)
        val isTracking = is_tracking.asStateFlow()

        private var is_on_route = MutableStateFlow(false)
        val isOnRoute = is_on_route.asStateFlow()

        private var route_path = MutableStateFlow<Polylines>(mutableListOf())
        val routePath = route_path.asStateFlow()

        private var total_time = MutableStateFlow(0L)
        val totalTime = total_time.asStateFlow()

    }

}

