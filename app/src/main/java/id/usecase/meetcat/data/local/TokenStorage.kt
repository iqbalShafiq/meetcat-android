package id.usecase.meetcat.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun hasToken(): Boolean {
        return getToken() != null
    }

    companion object {
        private const val PREFS_NAME = "meetcat_prefs"
        private const val KEY_TOKEN = "auth_token"
    }
}
