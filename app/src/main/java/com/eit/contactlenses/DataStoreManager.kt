import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

private val Context.dataStore by preferencesDataStore(name = "calendar_prefs")

class DataStoreManager(private val context: Context) {
    private val USED_DAYS_KEY = stringSetPreferencesKey("used_days")

    val usedDays: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[USED_DAYS_KEY] ?: emptySet()
        }

    suspend fun saveUsedDate(dateString: String) {
        context.dataStore.edit { preferences ->
            val updatedDays = preferences[USED_DAYS_KEY]?.toMutableSet() ?: mutableSetOf()
            updatedDays.add(dateString)
            preferences[USED_DAYS_KEY] = updatedDays
        }
    }

}