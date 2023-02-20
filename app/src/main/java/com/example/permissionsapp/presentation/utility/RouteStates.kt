package com.example.permissionsapp.presentation.utility

sealed class RouteStates{

    object RouteStarted : RouteStates()

    object RoutePaused : RouteStates()

    object RouteStopped: RouteStates()
}
