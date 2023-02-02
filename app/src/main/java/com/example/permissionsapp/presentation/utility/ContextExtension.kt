package com.example.permissionsapp.presentation.utility

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


fun Context.hasLocationPermission(): Boolean {
    val requiredPermissions: Array<String> = buildList {
        add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        add(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }.toTypedArray()

    val allGranted = requiredPermissions.all { permission ->
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    return allGranted
}