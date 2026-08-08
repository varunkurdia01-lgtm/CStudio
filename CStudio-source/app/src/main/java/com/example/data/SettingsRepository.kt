package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cstudio_settings", Context.MODE_PRIVATE)

    companion object {
        private var _themeFlow: MutableStateFlow<String>? = null
        
        fun getThemeFlow(context: Context): StateFlow<String> {
            if (_themeFlow == null) {
                val prefs = context.applicationContext.getSharedPreferences("cstudio_settings", Context.MODE_PRIVATE)
                val currentTheme = prefs.getString("theme", "System Default") ?: "System Default"
                _themeFlow = MutableStateFlow(currentTheme)
            }
            return _themeFlow!!
        }
    }

    var theme: String
        get() = prefs.getString("theme", "System Default") ?: "System Default"
        set(value) {
            prefs.edit().putString("theme", value).apply()
            _themeFlow?.value = value
        }

    var editorFontSize: Float
        get() = prefs.getFloat("editor_font_size", 14f)
        set(value) = prefs.edit().putFloat("editor_font_size", value).apply()

    var tabSize: Int
        get() = prefs.getInt("tab_size", 4)
        set(value) = prefs.edit().putInt("tab_size", value).apply()

    var wordWrap: Boolean
        get() = prefs.getBoolean("word_wrap", false)
        set(value) = prefs.edit().putBoolean("word_wrap", value).apply()

    var autoSave: Boolean
        get() = prefs.getBoolean("auto_save", true)
        set(value) = prefs.edit().putBoolean("auto_save", value).apply()
        
    var workspaceUri: String?
        get() = prefs.getString("workspace_uri", null)
        set(value) = prefs.edit().putString("workspace_uri", value).apply()

    var compiler: String
        get() = prefs.getString("compiler", "gcc-head") ?: "gcc-head"
        set(value) = prefs.edit().putString("compiler", value).apply()
        
    var openTabs: String
        get() = prefs.getString("open_tabs", "") ?: ""
        set(value) = prefs.edit().putString("open_tabs", value).apply()
        
    var activeTabIndex: Int
        get() = prefs.getInt("active_tab_index", -1)
        set(value) = prefs.edit().putInt("active_tab_index", value).apply()

    fun getOpenTabsList(): List<TabState> {
        val jsonString = prefs.getString("open_tabs_json", "[]") ?: "[]"
        val list = mutableListOf<TabState>()
        try {
            val jsonArray = org.json.JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(TabState(obj.getString("projectName"), obj.getString("fileName")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveOpenTabsList(tabs: List<TabState>) {
        val jsonArray = org.json.JSONArray()
        tabs.forEach { tab ->
            val obj = org.json.JSONObject()
            obj.put("projectName", tab.projectName)
            obj.put("fileName", tab.fileName)
            jsonArray.put(obj)
        }
        prefs.edit().putString("open_tabs_json", jsonArray.toString()).apply()
    }

    fun clearCache(context: Context) {
        context.cacheDir.deleteRecursively()
    }
}
