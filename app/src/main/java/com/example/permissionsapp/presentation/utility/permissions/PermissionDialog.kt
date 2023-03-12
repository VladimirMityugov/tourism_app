package com.example.permissionsapp.presentation.utility.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder


private const val TAG = "PERMISSIONS"
fun providePermissionDialog(
    context: Context,
    permissionDialogTextProvider: PermissionDialogTextProvider,
    isPermanentlyDeclined: Boolean,
    onOkClick: () -> Unit,
    onDismissClick: () -> Unit,
    onGoToAppSettingsCLick: () -> Unit
) {
Log.d(TAG, "SHOW DIALOG")
    MaterialAlertDialogBuilder(context)
        .setTitle("Permission dialog")
        .setMessage(permissionDialogTextProvider.getDialogText(isPermanentlyDeclined = isPermanentlyDeclined))
        .setPositiveButton(if(isPermanentlyDeclined)"Grant permission" else "OK") { _, _ ->
            if (isPermanentlyDeclined) {
                onGoToAppSettingsCLick()
            } else {
                onOkClick()
            }
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            onDismissClick()
            dialog.dismiss()
        }
        .create()
        .show()
}

interface PermissionDialogTextProvider {
    fun getDialogText(isPermanentlyDeclined: Boolean): String
}

class ReadExternalStoragePermission : PermissionDialogTextProvider {
    override fun getDialogText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Seems you permanently declined read permission so you can" +
                    "go to app settings to grant it"
        } else {
            "This app needs access to read external storage so that you can " +
                    "see saved photos"
        }
    }
}

class ReadMediaPermission : PermissionDialogTextProvider {
    override fun getDialogText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Seems you permanently declined read media permission so you can " +
                    "go to app settings to grant it"
        } else {
            "This app needs access to read media so that you can" +
                    "see saved photos"
        }
    }
}

class WriteExternalStoragePermission : PermissionDialogTextProvider {
    override fun getDialogText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Seems you permanently declined write permission so you can " +
                    "go to app settings to grant it"
        } else {
            "This app needs access to write external storage so that you can " +
                    "save your photos"
        }
    }
}

class CameraPermission : PermissionDialogTextProvider {
    override fun getDialogText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Seems you permanently declined camera permission so you can " +
                    "go to app settings to grant it"
        } else {
            "This app needs access to camera so that you can " +
                    "take photos on your routes"
        }
    }
}

class AccessCoarseLocationPermission : PermissionDialogTextProvider {
    override fun getDialogText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Seems you permanently declined coarse location permission so you can " +
                    "go to app settings to grant it"
        } else {
            "This app needs access to coarse location so that you can " +
                    "track yourself on route"
        }
    }
}

class AccessFineLocationPermission : PermissionDialogTextProvider {
    override fun getDialogText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Seems you permanently declined fine location permission so you can " +
                    "go to app settings to grant it"
        } else {
            "This app needs access to fine location so that you can " +
                    "track yourself on route"
        }
    }
}

class AccessBackgroundLocationPermission : PermissionDialogTextProvider {
    override fun getDialogText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Seems you permanently declined background location permission so you can " +
                    "go to app settings to grant it"
        } else {
            "This app needs access to background location so that you can " +
                    "track yourself on route"
        }
    }
}

class ForegroundServicePermission : PermissionDialogTextProvider {
    override fun getDialogText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Seems you permanently declined foreground service permission so you can " +
                    "go to app settings to grant it"
        } else {
            "This app needs access to foreground service so that you can " +
                    "continue your route tracking on hidden application"
        }
    }
}

class PostNotificationPermission : PermissionDialogTextProvider {
    override fun getDialogText(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Seems you permanently declined notification permission so you can " +
                    "go to app settings to grant it"
        } else {
            "This app needs access to notification so that you can " +
                    "always see your location whenever on route"
        }
    }
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also {
        startActivity(it)
    }
}