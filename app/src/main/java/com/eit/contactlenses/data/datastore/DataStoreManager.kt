import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "calendar_prefs")

class DataStoreManager(private val context: Context) {
    private val usedDaysKey = stringSetPreferencesKey("used_days")

    val usedDays: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[usedDaysKey] ?: emptySet()
        }

    suspend fun saveUsedDate(dateString: String) {
        context.dataStore.edit { preferences ->
            val updatedDays = preferences[usedDaysKey]?.toMutableSet() ?: mutableSetOf()
            updatedDays.add(dateString)
            preferences[usedDaysKey] = updatedDays
        }
    }
}