package com.example.tourismapp.presentation.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.room.Room
import com.example.tourismapp.data.local.MyDataBase
import com.example.tourismapp.data.remote.PlacesApi
import com.example.tourismapp.data.user_preferences.UserPreferences
import com.example.tourismapp.data.user_preferences.UserPreferencesSerializer
import com.example.tourismapp.presentation.utility.Constants.DATA_STORE_NAME
import com.example.tourismapp.presentation.utility.DefaultLocationClient
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
    fun providePhotoDatabase(@ApplicationContext context: Context): MyDataBase {
        return Room.databaseBuilder(
            context = context,
            klass = MyDataBase::class.java,
            name = "db"
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
    fun provideDefaultLocationClient(@ApplicationContext context: Context): DefaultLocationClient {
        return DefaultLocationClient(
            context = context,
            LocationServices.getFusedLocationProviderClient(context)
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
