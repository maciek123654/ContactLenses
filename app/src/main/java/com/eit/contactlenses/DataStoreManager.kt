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

    fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time) // "2024-04"
    }

    val usedDays: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            val allDays = preferences[USED_DAYS_KEY] ?: emptySet()
            val currentMonth = getCurrentMonth()
            allDays.filter { it.startsWith(currentMonth) }.toSet()
        }

    suspend fun saveUsedDay(day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        context.dataStore.edit { preferences ->
            val updatedDays = preferences[USED_DAYS_KEY]?.toMutableSet() ?: mutableSetOf()
            updatedDays.add(dateString)
            preferences[USED_DAYS_KEY] = updatedDays
        }
    }
}
