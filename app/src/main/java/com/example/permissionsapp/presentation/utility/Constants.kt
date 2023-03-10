package com.example.permissionsapp.presentation.utility

import android.Manifest
import android.graphics.Color
import android.os.Build

object Constants {

    const val ACTION_START = "ACTION_START"
    const val ACTION_STOP = "ACTION_STOP"
    const val ACTION_PAUSE = "ACTION_PAUSE"
    const val LOCATION_SERVICE_CHANNEL_ID = "location_channel"
    const val LOCATION_CHANNEL_NAME = "Tracking"
    const val LOCATION_NOTIFICATION_ID = 1
    const val INTERVAL_FOR_LOCATION_UPDATES = 2000L
    const val FIREBASE_NOTIFICATION_ID = 2
    const val FIREBASE_SERVICE_CHANNEL_ID = "firebase_channel"
    const val ACTION_SHOW_MAPS_FRAGMENT = "ACTION_SHOW_MAPS_FRAGMENT"
    const val REQUEST_CODE_LOCATION_PERMISSION = 0
    const val RATIONALE_FOR_LOCATION = "This app works only with location permission"
    const val CAMERA_ZOOM_VALUE = 18F
    const val POLYLINE_WIDTH = 24F
    const val POLYLINE_COLOR = Color.BLUE
    const val AVATAR = "https://w0.peakpx.com/wallpaper/458/418/HD-wallpaper-captain-jack-sparrow-fantasy-luminos-man-face-mark-armstrong-rand-johnny-depp.jpg"
    const val DATA_STORE_NAME = "DATA_STORE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_AVATAR_URL = "KEY_AVATAR_URL"
    const val KEY_FIRST_LAUNCH = "KEY_FIRST_LAUNCH"
    val REQUIRED_PERMISSIONS: Array<String> = buildList {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_AUDIO)
        }
    }.toTypedArray()

}