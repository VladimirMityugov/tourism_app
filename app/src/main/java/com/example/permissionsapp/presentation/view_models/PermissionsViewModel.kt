package com.example.permissionsapp.presentation.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


private const val TAG = "PERMISSION_VIEW"

class PermissionsViewModel: ViewModel() {

    private var _permissionsList = MutableStateFlow<List<String>>(emptyList())
    val permissionsList = _permissionsList.asStateFlow()


    fun dismissDialog(permission: String){
        val currentList = _permissionsList.value.toMutableList()
        currentList.remove(permission)
        _permissionsList.value = currentList
        Log.d(TAG, "PERMISSION LIST IS: ${_permissionsList.value}")
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if(!isGranted && !permissionsList.value.contains(permission)) {
            val currentList = _permissionsList.value.toMutableList()
            currentList.add(permission)
           _permissionsList.value = currentList
            Log.d(TAG, "PERMISSION LIST IS: ${_permissionsList.value} ")
        }
    }

}