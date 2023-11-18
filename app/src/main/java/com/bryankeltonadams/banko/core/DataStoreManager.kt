package com.bryankeltonadams.banko.core

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context
) {
    private val _sessionDataStore: DataStore<Preferences> by lazy {
        createDataStore("session")
    }
    var sessionDataStore: DataStore<Preferences> = _sessionDataStore
        private set
    private val _userDataStoreMap by lazy {
        ConcurrentHashMap<String, DataStore<Preferences>>()
    }

    fun getUserDataStore(email: String): DataStore<Preferences> =
        _userDataStoreMap.getOrPut(
            email.replace("@", "_").replace(".", "_")
        ) { createDataStore(email) }

    private fun createDataStore(dataStoreName: String): DataStore<Preferences> {
        return applicationContext.createDataStore(dataStoreName)
    }

    private fun Context.createDataStore(
        name: String,
        corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
        migrations: List<DataMigration<Preferences>> = listOf(),
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler = corruptionHandler,
            migrations = migrations,
            scope = scope
        ) {
            File(this.filesDir, "datastore/$name.preferences_pb")
        }
}
