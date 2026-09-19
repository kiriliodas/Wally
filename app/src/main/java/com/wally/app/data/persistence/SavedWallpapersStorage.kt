package com.wally.app.data.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wally.app.data.model.WallpaperItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wally_preferences")

class SavedWallpapersStorage(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        val KEY_SAVED_IDS = stringSetPreferencesKey("saved_wallpaper_ids")
        val KEY_SAVED_METADATA_JSON = stringPreferencesKey("saved_wallpapers_metadata_json")
        val KEY_LAST_CATEGORY = stringPreferencesKey("last_selected_category")
    }

    val savedIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_SAVED_IDS] ?: emptySet()
    }

    val savedWallpapers: Flow<List<WallpaperItem>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_SAVED_METADATA_JSON]
        if (!jsonStr.isNullOrBlank()) {
            try {
                json.decodeFromString<List<WallpaperItem>>(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    val lastCategory: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_CATEGORY] ?: "All"
    }

    suspend fun toggleSave(item: WallpaperItem) {
        context.dataStore.edit { prefs ->
            val currentIds = prefs[KEY_SAVED_IDS]?.toMutableSet() ?: mutableSetOf()
            val currentJsonStr = prefs[KEY_SAVED_METADATA_JSON]
            val currentItems = if (!currentJsonStr.isNullOrBlank()) {
                try {
                    json.decodeFromString<List<WallpaperItem>>(currentJsonStr).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            if (currentIds.contains(item.id)) {
                currentIds.remove(item.id)
                currentItems.removeAll { it.id == item.id }
            } else {
                currentIds.add(item.id)
                // Save metadata only (never raw image bytes)
                currentItems.add(0, item)
            }

            prefs[KEY_SAVED_IDS] = currentIds
            prefs[KEY_SAVED_METADATA_JSON] = json.encodeToString(currentItems)
        }
    }

    suspend fun saveLastCategory(category: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_CATEGORY] = category
        }
    }

    fun isSaved(id: String): Flow<Boolean> = savedIds.map { it.contains(id) }
}
