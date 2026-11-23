package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "montenegro_prefs")

class SessionPreferences(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val USER_JSON = stringPreferencesKey("montenegro_usuario")
        val LEMBRAR_ME = booleanPreferencesKey("montenegro_lembrar")
        val ULTIMO_EMAIL = stringPreferencesKey("montenegro_ultimo_email")
        val DARK_THEME = booleanPreferencesKey("theme_dark")
        val HAS_THEME_OVERRIDE = booleanPreferencesKey("theme_override")
        val NOTIF_VISTAS = intPreferencesKey("montenegro_notif_vistas")
        fun tourSeen(email: String) = booleanPreferencesKey("montenegro_tour_$email")
    }

    val darkThemeFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        if (prefs[Keys.HAS_THEME_OVERRIDE] == true) prefs[Keys.DARK_THEME] else null
    }

    val notifVistasFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIF_VISTAS] ?: 0
    }

    suspend fun getSavedSession(): UserSession? {
        val raw = context.dataStore.data.first()[Keys.USER_JSON] ?: return null
        return runCatching { json.decodeFromString<UserSession>(raw) }.getOrNull()
    }

    suspend fun getLembrarMe(): Boolean =
        context.dataStore.data.first()[Keys.LEMBRAR_ME] ?: true

    suspend fun getUltimoEmail(): String =
        context.dataStore.data.first()[Keys.ULTIMO_EMAIL].orEmpty()

    suspend fun saveSession(session: UserSession?, lembrar: Boolean, emailForRemember: String?) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LEMBRAR_ME] = lembrar
            if (lembrar && !emailForRemember.isNullOrBlank()) {
                prefs[Keys.ULTIMO_EMAIL] = emailForRemember
            } else if (!lembrar) {
                prefs.remove(Keys.ULTIMO_EMAIL)
            }
            if (session != null && lembrar) {
                prefs[Keys.USER_JSON] = json.encodeToString(session)
            } else {
                prefs.remove(Keys.USER_JSON)
            }
        }
    }

    suspend fun updatePersistedSessionIfRemembered(session: UserSession) {
        context.dataStore.edit { prefs ->
            if (prefs[Keys.LEMBRAR_ME] != false) {
                prefs[Keys.USER_JSON] = json.encodeToString(session)
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.USER_JSON)
        }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_THEME_OVERRIDE] = true
            prefs[Keys.DARK_THEME] = dark
        }
    }

    suspend fun setNotifVistas(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIF_VISTAS] = count
        }
    }

    suspend fun isTourSeen(email: String): Boolean =
        context.dataStore.data.first()[Keys.tourSeen(email)] ?: false

    suspend fun markTourSeen(email: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.tourSeen(email)] = true
        }
    }
}
