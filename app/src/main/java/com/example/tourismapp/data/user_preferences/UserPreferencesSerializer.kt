package com.example.tourismapp.data.user_preferences

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.io.OutputStream


object UserPreferencesSerializer: Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): UserPreferences {
       return try {
           Json.decodeFromStream(input)
       }catch (e: SerializationException){
           e.stackTrace
           defaultValue
       }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = UserPreferences.serializer(),
                    value = t
                ).toByteArray()
            )
        }
    }
}