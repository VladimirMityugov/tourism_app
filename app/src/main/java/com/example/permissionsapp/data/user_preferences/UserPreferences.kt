package com.example.permissionsapp.data.user_preferences

@kotlinx.serialization.Serializable
data class UserPreferences(
    val user_name: String = "",
    val user_avatar_uri: String = "",
    val isFirstLaunch: Boolean = true
)
