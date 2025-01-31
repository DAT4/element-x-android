/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.rageshake.crash

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_crash")

private val appHasCrashedKey = booleanPreferencesKey("appHasCrashed")
private val crashDataKey = stringPreferencesKey("crashData")

class CrashDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = context.dataStore

    fun setCrashData(crashData: String) {
        // Must block
        runBlocking {
            store.edit { prefs ->
                prefs[appHasCrashedKey] = true
                prefs[crashDataKey] = crashData
            }
        }
    }

    suspend fun resetAppHasCrashed() {
        store.edit { prefs ->
            prefs[appHasCrashedKey] = false
        }
    }

    fun appHasCrashed(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[appHasCrashedKey].orFalse()
        }
    }

    fun crashInfo(): Flow<String> {
        return store.data.map { prefs ->
            prefs[crashDataKey].orEmpty()
        }
    }

    suspend fun reset() {
        store.edit { it.clear() }
    }
}
