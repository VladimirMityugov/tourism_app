package com.example.tourismapp.presentation.di

import com.example.tourismapp.data.local.dao.ObjectDao
import com.example.tourismapp.data.local.dao.PhotoDao
import com.example.tourismapp.data.local.dao.PlacesKindsDao
import com.example.tourismapp.data.local.dao.RouteDao
import com.example.tourismapp.data.remote.PlacesApi
import com.example.tourismapp.data.repositories.repository_local.RepositoryObjectLocalImpl
import com.example.tourismapp.data.repositories.repository_local.RepositoryPhotoLocalImpl
import com.example.tourismapp.data.repositories.repository_local.RepositoryPlacesLocalImpl
import com.example.tourismapp.data.repositories.repository_local.RepositoryRouteLocalImpl
import com.example.tourismapp.data.repositories.repository_remote.RepositoryRemoteImpl
import com.example.tourismapp.domain.repositories.repository_local.RepositoryObjectLocal
import com.example.tourismapp.domain.repositories.repository_local.RepositoryPhotoLocal
import com.example.tourismapp.domain.repositories.repository_local.RepositoryPlacesLocal
import com.example.tourismapp.domain.repositories.repository_local.RepositoryRouteLocal
import com.example.tourismapp.domain.repositories.repository_remote.RepositoryRemote
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)

object DomainModule {

    @Provides
    @ViewModelScoped
    fun provideRepositoryRemote(placesApi: PlacesApi):RepositoryRemote{
        return RepositoryRemoteImpl(placesApi)
    }

    @Provides
    @ViewModelScoped
    fun provideRepositoryObjectLocal(objectDao: ObjectDao):RepositoryObjectLocal{
        return RepositoryObjectLocalImpl(objectDao)
    }

    @Provides
    @ViewModelScoped
    fun provideRepositoryPhotoLocal(photoDao: PhotoDao):RepositoryPhotoLocal{
        return RepositoryPhotoLocalImpl(photoDao)
    }

    @Provides
    @ViewModelScoped
    fun provideRepositoryPlacesLocal(placesKindsDao: PlacesKindsDao): RepositoryPlacesLocal {
        return RepositoryPlacesLocalImpl(placesKindsDao)
    }

    @Provides
    @ViewModelScoped
    fun provideRepositoryRouteLocal(routeDao: RouteDao): RepositoryRouteLocal {
        return RepositoryRouteLocalImpl(routeDao)
    }



}