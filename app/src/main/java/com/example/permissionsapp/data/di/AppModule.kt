package com.example.permissionsapp.data.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.permissionsapp.data.local.MyDataBase
import com.example.permissionsapp.data.remote.PlacesApi
import com.example.permissionsapp.presentation.utility.DefaultLocationClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Provides
    @ViewModelScoped
    fun providePhotoDatabase(app: Application): MyDataBase {
        return Room.databaseBuilder(
            app.applicationContext,
            MyDataBase::class.java,
            "db"
        ).build()
    }

    @Provides
    @ViewModelScoped
    fun providePhotoDao(dataBase: MyDataBase) = dataBase.photoDao

    @Provides
    @ViewModelScoped
    fun provideObjectDao(dataBase: MyDataBase) = dataBase.objectDao

    @Provides
    @ViewModelScoped
    fun providePlacesKindsDao(dataBase: MyDataBase) = dataBase.placesKindsDao

    @Provides
    @ViewModelScoped
    fun provideRouteDataDao(dataBase: MyDataBase) = dataBase.routeDao

    @Provides
    @ViewModelScoped
    fun provideDefaultLocationClient(app: Application): DefaultLocationClient {
        return DefaultLocationClient(
            app.applicationContext,
            LocationServices.getFusedLocationProviderClient(app.applicationContext)
        )
    }

    @Provides
    @ViewModelScoped
    fun providePlacesApi(): PlacesApi {
        return Retrofit.Builder()
            .baseUrl(PlacesApi.BASE_URl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PlacesApi::class.java)
    }

}
