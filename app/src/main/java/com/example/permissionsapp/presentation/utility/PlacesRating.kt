package com.example.permissionsapp.presentation.utility

sealed class PlacesRating {
    object LOW : PlacesRating()
    object MEDIUM : PlacesRating()
    object HIGH : PlacesRating()
    object LOW_H : PlacesRating()
    object MEDIUM_H : PlacesRating()
    object HIGH_H : PlacesRating()
}