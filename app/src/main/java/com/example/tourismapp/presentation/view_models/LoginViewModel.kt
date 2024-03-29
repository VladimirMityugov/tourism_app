package com.example.tourismapp.presentation.view_models

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourismapp.data.user_preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
  private val dataStore: DataStore<UserPreferences>
): ViewModel() {


  private val _isEmailEntered = MutableStateFlow(false)
  val isEmailEntered = _isEmailEntered.asStateFlow()

  private val _isPasswordEntered = MutableStateFlow(false)
  val isPasswordEntered = _isPasswordEntered.asStateFlow()

  private val _isNewEmailEntered = MutableStateFlow(false)
  val isNewEmailEntered = _isNewEmailEntered.asStateFlow()

  private val _isNewPasswordEntered = MutableStateFlow(false)
  val isNewPasswordEntered = _isNewPasswordEntered.asStateFlow()

  private val _isNameEntered = MutableStateFlow(false)
  val isNameEntered = _isNameEntered.asStateFlow()


  //LoginActivity

  fun switchEmailStatus(status: Boolean) {
    viewModelScope.launch {
      _isEmailEntered.value = status
    }
  }

  fun switchPasswordStatus(status: Boolean) {
    viewModelScope.launch {
      _isPasswordEntered.value = status
    }
  }

  fun switchRegistrationEmailStatus(status: Boolean) {
    viewModelScope.launch {
      _isNewEmailEntered.value = status
    }
  }

  fun switchRegistrationPasswordStatus(status: Boolean) {
    viewModelScope.launch {
      _isNewPasswordEntered.value = status
    }
  }

  fun switchNameStatus(status: Boolean) {
    viewModelScope.launch {
      _isNameEntered.value = status
    }
  }

  fun saveNameToDataStore(name: String){
    viewModelScope.launch {
     dataStore.updateData {
       it.copy(
         user_name = name
       )
     }
    }
  }

  fun updateLaunchStatus(isFirstLaunch: Boolean){
    viewModelScope.launch {
    dataStore.updateData {
      it.copy(
        isFirstLaunch = isFirstLaunch
      )
    }
    }
  }


}