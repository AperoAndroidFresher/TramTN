package com.example.musicapp.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageCode = getSavedLanguage(newBase)
        val newContext = updateLocale(newBase, languageCode)
        super.attachBaseContext(newContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setLocale(languageCode: String) {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("App_Lang", languageCode).apply()

        recreate()
    }

    private fun getSavedLanguage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("App_Lang", "en") ?: "en"
    }

    private fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
