package id.usecase.meetcat.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStorage(context: Context) {

    private val prefs: SharedPreferences = try {
        // Create or retrieve the master key for encryption
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Create encrypted shared preferences
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to regular SharedPreferences if encryption fails
        // This can happen on devices with compromised security or during testing
        Log.w(TAG, "EncryptedSharedPreferences failed, falling back to regular SharedPreferences", e)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        Log.d(TAG, "Saving token: ${token.take(10)}...") // Only log first 10 chars for security
        prefs.edit().putString(KEY_TOKEN, token).apply()
        Log.d(TAG, "Token saved successfully. Has token: ${hasToken()}")
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        Log.d(TAG, "Getting token: ${if (token != null) "${token.take(10)}..." else "null"}")
        return token
    }

    fun clearToken() {
        Log.d(TAG, "Clearing token")
        prefs.edit().remove(KEY_TOKEN).apply()
        Log.d(TAG, "Token cleared. Has token: ${hasToken()}")
    }

    fun hasToken(): Boolean {
        val has = getToken() != null
        Log.d(TAG, "Has token: $has")
        return has
    }

    companion object {
        private const val TAG = "TokenStorage"
        private const val PREFS_NAME = "meetcat_secure_prefs"
        private const val KEY_TOKEN = "auth_token"
    }
}
