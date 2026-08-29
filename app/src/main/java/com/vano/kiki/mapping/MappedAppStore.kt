package com.vano.kiki.mapping

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object MappedAppStore {
    private const val PREFS = "kiki_mapped_apps"
    private const val KEY_APPS = "apps"

    fun load(context: Context): List<AppInfoUi> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_APPS, null) ?: return emptyList()
        val array = JSONArray(raw)
        val result = mutableListOf<AppInfoUi>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(AppInfoUi(label = obj.getString("label"), packageName = obj.getString("pkg")))
        }
        return result
    }

    fun save(context: Context, apps: List<AppInfoUi>) {
        val array = JSONArray()
        apps.forEach { app ->
            val obj = JSONObject()
            obj.put("label", app.label)
            obj.put("pkg", app.packageName)
            array.put(obj)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APPS, array.toString())
            .apply()
    }
}
