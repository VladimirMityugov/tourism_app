package com.example.permissionsapp.presentation.utility.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_CAMERA_PERMISSIONS
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_FOREGROUND_PERMISSIONS
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_LOCATION_PERMISSIONS
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_NOTIFICATION_PERMISSIONS
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_READ_PERMISSIONS
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_WRITE_PERMISSIONS

private const val TAG = "LOCATION_PERMISSION"
fun Context.hasLocationPermission(): Boolean {
    return REQUIRED_LOCATION_PERMISSIONS.all { permission ->
        Log.d(TAG, "PERMISSION: $permission")
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.hasReadPermission(): Boolean {
    return REQUIRED_READ_PERMISSIONS.all { permission ->
        Log.d(TAG, "PERMISSION TO BE GRANTED: $permission")
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.hasWritePermission(): Boolean {
    return REQUIRED_WRITE_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.hasNotificationPermission(): Boolean {
    return REQUIRED_NOTIFICATION_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.hasForegroundServicePermission(): Boolean {
    return REQUIRED_FOREGROUND_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.hasCameraPermission(): Boolean {
    return REQUIRED_CAMERA_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

}


