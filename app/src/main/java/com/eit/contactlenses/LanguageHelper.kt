package com.eit.contactlenses

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageHelper {
    private const val PREF_NAME = "app_prefs"
    private const val IS_FIRST_LAUNCH_KEY = "is_first_launch"
    private const val LANGUAGE_KEY = "language"

    fun isFirstLaunch(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(IS_FIRST_LAUNCH_KEY, true)
    }

    fun setFirstLaunchCompleted(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(IS_FIRST_LAUNCH_KEY, false)
        editor.apply()
    }

    fun setLanguage(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        val newContext = context.createConfigurationContext(config)

        saveLanguagePreference(context, languageCode)

        return newContext
    }

    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(LANGUAGE_KEY, languageCode)
        editor.apply()
    }

    fun getSavedLanguage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LANGUAGE_KEY, Locale.getDefault().language) ?: "en"
    }

    fun applySavedLanguage(context: Context): Context {
        val languageCode = getSavedLanguage(context)
        return setLanguage(context, languageCode)
    }

    fun getString(context: Context, key: String): String{
        val langCode = getSavedLanguage(context)
        val resourceId = context.resources.getIdentifier("${key}_$langCode", "string", context.packageName)
        return if (resourceId != 0) context.getString(resourceId) else key
    }
}
