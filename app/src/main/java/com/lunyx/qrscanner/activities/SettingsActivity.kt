package com.lunyx.qrscanner.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.lunyx.qrscanner.R
import com.lunyx.qrscanner.utils.LocaleHelper
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var spinnerTheme: Spinner
    private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerFormat: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences("qr_prefs", MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        spinnerTheme = findViewById(R.id.spinnerTheme)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        spinnerFormat = findViewById(R.id.spinnerFormat)

        setupThemeSpinner()
        setupLanguageSpinner()
        setupFormatSpinner()

        findViewById<MaterialButton>(R.id.btnSaveSettings).setOnClickListener {
            saveSettings()
        }
    }

    private fun setupThemeSpinner() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTheme.adapter = adapter
        val current = when (prefs.getString("theme", "light")) {
            "dark" -> 1
            "system" -> 2
            else -> 0
        }
        spinnerTheme.setSelection(current)
    }

    private fun setupLanguageSpinner() {
        val langs = arrayOf("Українська", "English", "Deutsch")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, langs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter
        val current = when (prefs.getString("language", "uk")) {
            "en" -> 1
            "de" -> 2
            else -> 0
        }
        spinnerLanguage.setSelection(current)
    }

    private fun setupFormatSpinner() {
        val formats = arrayOf("PNG", "JPG", "WEBP")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, formats)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFormat.adapter = adapter
        val current = when (prefs.getString("format", "PNG")) {
            "JPG" -> 1
            "WEBP" -> 2
            else -> 0
        }
        spinnerFormat.setSelection(current)
    }

    private fun saveSettings() {
        val theme = when (spinnerTheme.selectedItemPosition) {
            1 -> "dark"
            2 -> "system"
            else -> "light"
        }
        val language = when (spinnerLanguage.selectedItemPosition) {
            1 -> "en"
            2 -> "de"
            else -> "uk"
        }
        val format = when (spinnerFormat.selectedItemPosition) {
            1 -> "JPG"
            2 -> "WEBP"
            else -> "PNG"
        }

        prefs.edit()
            .putString("theme", theme)
            .putString("language", language)
            .putString("format", format)
            .apply()

        when (theme) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        LocaleHelper.applyLocale(this, language)
        finish()
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
