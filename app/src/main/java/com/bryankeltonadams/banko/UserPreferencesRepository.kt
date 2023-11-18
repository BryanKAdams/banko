package com.bryankeltonadams.banko

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bryankeltonadams.banko.core.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager,
) {

    val sessionPreferencesFlow: Flow<SessionPreferences> =
        dataStoreManager.sessionDataStore.data.map { preferences ->
            SessionPreferences(preferences)
        }

    suspend fun setName(name: String) {
        dataStoreManager.sessionDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[SessionPreferences.NAME] = name
            }
        }
    }

    suspend fun setGameCode(gameCode: String) {
        dataStoreManager.sessionDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[SessionPreferences.GAME_CODE] = gameCode
            }
        }
    }

}

data class SessionPreferences(
    val name: String = "",
    val gameCode: String = "",
) {
    companion object {
        val NAME = stringPreferencesKey("name")
        val GAME_CODE = stringPreferencesKey("game_code")
    }

    constructor(preferences: Preferences) : this(
        name = preferences[NAME] ?: "",
        gameCode = preferences[GAME_CODE] ?: "",
    )
}