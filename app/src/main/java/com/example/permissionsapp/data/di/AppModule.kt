package com.example.permissionsapp.data.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.room.Room
import com.example.permissionsapp.data.local.MyDataBase
import com.example.permissionsapp.data.remote.PlacesApi
import com.example.permissionsapp.data.user_preferences.UserPreferences
import com.example.permissionsapp.data.user_preferences.UserPreferencesSerializer
import com.example.permissionsapp.presentation.utility.Constants.DATA_STORE_NAME
import com.example.permissionsapp.presentation.utility.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePhotoDatabase(app: Application): MyDataBase {
        return Room.databaseBuilder(
            app.applicationContext,
            MyDataBase::class.java,
            "db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePhotoDao(dataBase: MyDataBase) = dataBase.photoDao

    @Provides
    @Singleton
    fun provideObjectDao(dataBase: MyDataBase) = dataBase.objectDao

    @Provides
    @Singleton
    fun providePlacesKindsDao(dataBase: MyDataBase) = dataBase.placesKindsDao

    @Provides
    @Singleton
    fun provideRouteDataDao(dataBase: MyDataBase) = dataBase.routeDao

    @Provides
    @Singleton
    fun provideDefaultLocationClient(app: Application): DefaultLocationClient {
        return DefaultLocationClient(
            app.applicationContext,
            LocationServices.getFusedLocationProviderClient(app.applicationContext)
        )
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<UserPreferences> {
        return DataStoreFactory.create(
            produceFile = {context.dataStoreFile(DATA_STORE_NAME)},
            serializer = UserPreferencesSerializer
        )
    }

    @Provides
    @Singleton
    fun providePlacesApi(): PlacesApi {
        return Retrofit.Builder()
            .baseUrl(PlacesApi.BASE_URl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PlacesApi::class.java)
    }

}
