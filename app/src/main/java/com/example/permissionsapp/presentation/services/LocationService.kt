package com.example.permissionsapp.presentation.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.MutableLiveData
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
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*

private const val TAG = "LOCATION_SERVICE"
typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>
class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private var isFirstLaunch = true


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
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
//        addEmptyCoordinates()
        is_tracking.value = false
    }

    private fun resume() {
        is_tracking.value = true
    }

    private fun start() {

//       addEmptyCoordinates()
        addEmptyPolyline()
        is_tracking.value = true

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
                if (is_tracking.value) {
                    addPathPoint(location)
//                    addRoutePathPoint(location)
//                    Log.d(TAG, "ROUTE IS : ${route_path.value}")
                }
            }
            .launchIn(serviceScope)

        //Start service with notifications
        startForeground(LOCATION_NOTIFICATION_ID, notification.build())
    }

    private fun stop() {
        //Stop service
        is_tracking.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun postInitialValues() {
        pathPoints.postValue(mutableListOf())
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

//    private fun addEmptyCoordinates() {
//        Log.d(TAG, "ADD EMPTY COORDINATES. CURRENT LIST IS : ${route_path.value}")
//        val currentRouthPoints = routePath.value.toMutableList()
//        currentRouthPoints.add(null)
//        route_path.value = currentRouthPoints
//    }
//
//    private fun addRoutePathPoint(location: Location) {
//        val newPoint = LatLng(location.latitude, location.longitude)
//        val currentRouthPoints = routePath.value.toMutableList()
//        currentRouthPoints.add(newPoint)
//        route_path.value = currentRouthPoints
//    }
//
//    private fun addIdlePoint(location: Location) {
//        location.let {
//            val newPoint = LatLng(location.latitude, location.longitude)
//            val currentIdlePath = idle_path.value.toMutableList()
//            currentIdlePath.add(newPoint)
//            idle_path.value = currentIdlePath
//            Log.d(TAG, "IDLE IS : ${idle_path.value}")
//        }
//    }

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

        //        private var route_path = MutableStateFlow<List<LatLng?>>(emptyList())
//        val routePath = route_path.asStateFlow()
//
//        private var idle_path = MutableStateFlow<List<LatLng?>>(emptyList())
//        val idlePath = idle_path.asStateFlow()
        val pathPoints = MutableLiveData<Polylines>()
    }

}

