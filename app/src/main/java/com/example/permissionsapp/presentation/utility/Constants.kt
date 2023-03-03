package com.example.permissionsapp.presentation.utility

import android.graphics.Color

object Constants {

    const val ACTION_START = "ACTION_START"
    const val ACTION_STOP = "ACTION_STOP"
    const val ACTION_PAUSE = "ACTION_PAUSE"
    const val LOCATION_SERVICE_CHANNEL_ID = "location_channel"
    const val LOCATION_CHANNEL_NAME = "Tracking"
    const val LOCATION_NOTIFICATION_ID = 1
    const val INTERVAL_FOR_LOCATION_UPDATES = 5000L
    const val FIREBASE_NOTIFICATION_ID = 2
    const val FIREBASE_SERVICE_CHANNEL_ID = "firebase_channel"
    const val ACTION_SHOW_MAPS_FRAGMENT = "ACTION_SHOW_MAPS_FRAGMENT"
    const val REQUEST_CODE_LOCATION_PERMISSION = 0
    const val RATIONALE_FOR_LOCATION = "This app works only with location permission"
    const val CAMERA_ZOOM_VALUE = 18F
    const val POLYLINE_WIDTH = 24F
    const val POLYLINE_COLOR = Color.BLUE
    const val POLYLINE_IDLE_WIDTH = 12F
    const val POLYLINE_IDLE_COLOR = Color.RED

}