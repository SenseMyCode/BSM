package com.example.secretlab.data

import android.content.Context

class InsecureSessionStore(context: Context) {
    private val box = context.getSharedPreferences(CRATE_NAME, Context.MODE_PRIVATE)
//var secret: String? = null
    fun cacheOpenSecret(value: String) {
        box.edit().putString(SLOT_FOREST, value).apply()
    }

    fun cacheTravelCard(value: String) {
        box.edit().putString(SLOT_HARBOR, value).apply()
    }

    fun readOpenSecret(): String? = box.getString(SLOT_FOREST, null)

    fun readTravelCard(): String? = box.getString(SLOT_HARBOR, null)

    fun clearAll() {
        box.edit().clear().apply()
    }

    companion object {
        const val CRATE_NAME = "night_archive"
        const val SLOT_FOREST = "fern_lane"
        const val SLOT_HARBOR = "blue_harbor"
    }
}
