package id.usecase.meetcat.presentation.screen.videoeditor

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

/**
 * Helper class to manage video editor assets
 * Handles loading backgrounds, cat objects, and sounds from assets folder
 */
class VideoEditorAssetManager(private val context: Context) {

    companion object {
        private const val BACKGROUNDS_PATH = "backgrounds"
        private const val CATS_PATH = "cats"
        private const val SOUNDS_PATH = "sounds"
    }

    /**
     * Get list of available background files from assets
     */
    fun getBackgrounds(): List<BackgroundAsset> {
        return try {
            val assetManager = context.assets
            val files = assetManager.list(BACKGROUNDS_PATH) ?: emptyArray()

            files.mapNotNull { fileName ->
                val assetPath = "$BACKGROUNDS_PATH/$fileName"
                val uri = getAssetUri(assetPath)

                BackgroundAsset(
                    id = fileName.substringBeforeLast("."),
                    name = formatAssetName(fileName),
                    uri = uri,
                    thumbnailUri = uri, // Same as uri for images
                    type = when (fileName.substringAfterLast(".").lowercase()) {
                        "mp4", "mov", "avi" -> BackgroundType.VIDEO
                        else -> BackgroundType.IMAGE
                    }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get list of available cat objects from assets
     * Each cat object will have a default sound attached
     */
    fun getCatObjects(): List<CatObject> {
        return try {
            val assetManager = context.assets
            val files = assetManager.list(CATS_PATH) ?: emptyArray()

            // Get available sounds to assign as defaults
            val availableSounds = getSounds()

            files.mapIndexed { index, fileName ->
                val assetPath = "$CATS_PATH/$fileName"
                val uri = getAssetUri(assetPath)

                // Assign default sound (cycle through available sounds, or try to match by name)
                val defaultSound = if (availableSounds.isNotEmpty()) {
                    // Try to find matching sound by name first (e.g., "dancing_cat" -> "cat_dancing" or "meow")
                    val catBaseName = fileName.substringBeforeLast(".").lowercase()
                    availableSounds.find {
                        it.name.lowercase().contains("cat") ||
                        it.name.lowercase().contains("meow") ||
                        it.name.lowercase().contains("purr")
                    } ?: availableSounds[index % availableSounds.size] // Fallback to cycling
                } else null

                CatObject(
                    id = fileName.substringBeforeLast("."),
                    name = formatAssetName(fileName),
                    thumbnailUri = uri.toString(),
                    resourceUri = uri.toString(),
                    type = when (fileName.substringAfterLast(".").lowercase()) {
                        "gif" -> CatObjectType.GIF
                        "mp4", "mov" -> CatObjectType.VIDEO
                        else -> CatObjectType.GIF
                    },
                    defaultSoundUri = defaultSound?.uri,
                    defaultSoundName = defaultSound?.name
                )
            }
        } catch (e: Exception) {
            // Return empty list if assets folder doesn't exist or is empty
            emptyList()
        }
    }

    /**
     * Get list of available sound files from assets
     */
    fun getSounds(): List<SoundAsset> {
        return try {
            val assetManager = context.assets
            val files = assetManager.list(SOUNDS_PATH) ?: emptyArray()

            files.mapNotNull { fileName ->
                val assetPath = "$SOUNDS_PATH/$fileName"
                val uri = getAssetUri(assetPath)

                SoundAsset(
                    id = fileName.substringBeforeLast("."),
                    name = formatAssetName(fileName),
                    uri = uri,
                    duration = 0L // Will be calculated when loaded
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get Uri for an asset file
     * Format: file:///android_asset/path/to/file
     */
    private fun getAssetUri(assetPath: String): Uri {
        return Uri.parse("file:///android_asset/$assetPath")
    }

    /**
     * Copy asset to cache directory and return file Uri
     * Useful for APIs that don't support android_asset:// URIs
     */
    fun copyAssetToCache(assetPath: String): Uri? {
        return try {
            val inputStream = context.assets.open(assetPath)
            val fileName = assetPath.substringAfterLast("/")
            val outputFile = File(context.cacheDir, fileName)

            outputFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            outputFile.toUri()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format asset file name to human-readable name
     * Example: "dancing_cat.gif" -> "Dancing Cat"
     */
    private fun formatAssetName(fileName: String): String {
        return fileName
            .substringBeforeLast(".")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }

    /**
     * Check if assets folder exists and has files
     */
    fun hasAssets(): Boolean {
        return try {
            val backgrounds = context.assets.list(BACKGROUNDS_PATH)?.isNotEmpty() ?: false
            val cats = context.assets.list(CATS_PATH)?.isNotEmpty() ?: false
            backgrounds || cats
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Represents a background asset
 */
data class BackgroundAsset(
    val id: String,
    val name: String,
    val uri: Uri,
    val thumbnailUri: Uri,
    val type: BackgroundType
)

enum class BackgroundType {
    IMAGE,
    VIDEO
}

/**
 * Represents a sound asset
 */
data class SoundAsset(
    val id: String,
    val name: String,
    val uri: Uri,
    val duration: Long
)
