# 🎬 Video Editor Assets Setup Guide

Panduan lengkap untuk menambahkan assets (background, cat objects, sounds) ke Video Editor screen.

---

## 📁 Struktur Folder Assets

Buat struktur folder berikut di project Anda:

```
meetcat-android/
└── app/
    └── src/
        └── main/
            └── assets/          ← Buat folder ini
                ├── backgrounds/  ← Background images/videos
                ├── cats/         ← Cat GIFs/videos (no background)
                └── sounds/       ← Audio files
```

---

## 🖼️ 1. Background Assets (assets/backgrounds/)

**Format yang didukung:**
- **Images**: `.jpg`, `.jpeg`, `.png`, `.webp`
- **Videos**: `.mp4`, `.mov`, `.avi`

**Contoh file:**
```
assets/backgrounds/
├── bg_gradient_purple.jpg
├── bg_gradient_blue.jpg
├── bg_pattern_dots.png
├── bg_space_stars.mp4
└── bg_nature_forest.mp4
```

**Naming Convention:**
- Gunakan `snake_case` (lowercase dengan underscore)
- File `bg_gradient_purple.jpg` akan muncul sebagai **"Bg Gradient Purple"**
- Nama otomatis di-format menjadi Title Case

**Rekomendasi:**
- **Resolusi**: 1920x1080 (Full HD) atau 1280x720 (HD)
- **Format**: JPG untuk static, MP4 untuk animated
- **Ukuran**: < 5MB per file untuk performa optimal

---

## 🐱 2. Cat Objects (assets/cats/)

**Format yang didukung:**
- **GIF**: `.gif`
- **Video**: `.mp4`, `.mov`

**PENTING:** File harus **transparan** atau **sudah dihapus backgroundnya**!

**Contoh file:**
```
assets/cats/
├── dancing_cat.gif
├── jumping_cat.gif
├── sleeping_cat.gif
├── running_cat.mp4
└── playing_cat.gif
```

**Naming Convention:**
- Gunakan `snake_case`
- File `dancing_cat.gif` akan muncul sebagai **"Dancing Cat"**

**Rekomendasi:**
- **Resolusi**: 512x512 atau 1024x1024
- **Background**: Transparan (GIF dengan transparency atau video dengan alpha channel)
- **Ukuran**: < 2MB per file
- **Loop**: Sebaiknya looping seamless

**Cara mendapatkan cat GIF transparan:**
1. [GIPHY](https://giphy.com) - Filter by transparent
2. [Tenor](https://tenor.com) - Cari "transparent cat"
3. Buat sendiri dengan tools:
   - [Remove.bg](https://www.remove.bg) - Hapus background otomatis
   - [Unscreen.com](https://www.unscreen.com) - Hapus background dari video/GIF

---

## 🎵 3. Sound Assets (assets/sounds/)

**Format yang didukung:**
- **Audio**: `.mp3`, `.wav`, `.m4a`, `.ogg`

**Contoh file:**
```
assets/sounds/
├── background_music_chill.mp3
├── background_music_upbeat.mp3
├── meow_cute.mp3
├── purr_soft.mp3
└── cat_meow_happy.wav
```

**Naming Convention:**
- Gunakan `snake_case`
- File `background_music_chill.mp3` akan muncul sebagai **"Background Music Chill"**

**Rekomendasi:**
- **Format**: MP3 (best compatibility)
- **Bitrate**: 128kbps - 320kbps
- **Durasi**: 10-30 detik untuk sound effects, 1-3 menit untuk background music
- **Ukuran**: < 3MB per file

**Sumber royalty-free music:**
1. [YouTube Audio Library](https://www.youtube.com/audiolibrary)
2. [Free Music Archive](https://freemusicarchive.org)
3. [Incompetech](https://incompetech.com/music/royalty-free/)
4. [Pixabay Music](https://pixabay.com/music/)

---

## 🚀 Cara Menambahkan Assets

### Via Android Studio:

1. **Buat folder assets** (jika belum ada):
   - Klik kanan pada `app/src/main/`
   - New → Directory
   - Ketik: `assets`
   - OK

2. **Buat subfolder**:
   - Klik kanan pada `assets/`
   - New → Directory
   - Buat 3 folder: `backgrounds`, `cats`, `sounds`

3. **Copy files**:
   - Drag & drop file ke folder yang sesuai
   - Atau: Klik kanan folder → Reveal in Finder/Explorer
   - Copy paste file secara manual

### Via Terminal/Command Line:

```bash
# Masuk ke root project
cd /path/to/meetcat-android

# Buat folder struktur
mkdir -p app/src/main/assets/backgrounds
mkdir -p app/src/main/assets/cats
mkdir -p app/src/main/assets/sounds

# Copy files (contoh)
cp ~/Downloads/dancing_cat.gif app/src/main/assets/cats/
cp ~/Downloads/bg_gradient.jpg app/src/main/assets/backgrounds/
cp ~/Downloads/music_chill.mp3 app/src/main/assets/sounds/
```

---

## ✅ Verifikasi Assets

Setelah menambahkan file, assets akan otomatis terdeteksi oleh `VideoEditorAssetManager`.

**Cara test:**
1. Build dan run aplikasi
2. Navigate ke tab **"Near Me"** (Video Editor)
3. Tap tombol di bottom sheet:
   - **Background** tab → Lihat background yang tersedia
   - **Objects** tab → Lihat cat objects
   - **Audio** tab → Lihat sounds

**Jika assets tidak muncul:**
- Clean project: `Build → Clean Project`
- Rebuild: `Build → Rebuild Project`
- Restart Android Studio

---

## 🎨 Contoh Assets Lengkap

Berikut contoh assets minimal untuk testing:

```
assets/
├── backgrounds/
│   ├── bg_blue_gradient.jpg       (200KB)
│   ├── bg_pink_gradient.jpg       (180KB)
│   └── bg_simple_pattern.png      (150KB)
├── cats/
│   ├── cat_dancing.gif            (800KB)
│   ├── cat_jumping.gif            (600KB)
│   └── cat_sleeping.gif           (500KB)
└── sounds/
    ├── meow_1.mp3                 (50KB)
    ├── meow_2.mp3                 (60KB)
    └── music_background.mp3       (2MB)
```

**Total size:** ~4.5 MB

---

## 📱 Cara Kerja di Aplikasi

### 1. Background Tab
- Menampilkan **grid horizontal** dari bundled backgrounds
- Tombol **"Choose from Gallery"** untuk ambil dari galeri user
- **Alternatif:** Bisa disambungkan ke Camera Screen untuk capture langsung

### 2. Objects Tab
- Menampilkan **grid horizontal** cat objects
- **Hanya** bundled assets (tidak ada picker eksternal)
- User tinggal tap untuk menambahkan ke timeline

### 3. Audio Tab
- Menampilkan **list vertical** sounds
- Tombol **"Choose Audio File"** untuk ambil dari file manager
- User bisa pilih bundled atau file sendiri

---

## 🔧 Customization

Jika ingin menambahkan lebih banyak jenis assets, edit file:
`app/src/main/java/id/usecase/meetcat/presentation/screen/videoeditor/AssetManager.kt`

**Contoh: Tambah stickers folder:**

```kotlin
companion object {
    private const val BACKGROUNDS_PATH = "backgrounds"
    private const val CATS_PATH = "cats"
    private const val SOUNDS_PATH = "sounds"
    private const val STICKERS_PATH = "stickers"  // ← Tambahkan ini
}

fun getStickers(): List<StickerAsset> {
    return try {
        val files = context.assets.list(STICKERS_PATH) ?: emptyArray()
        files.map { fileName ->
            // Map to StickerAsset
        }
    } catch (e: Exception) {
        emptyList()
    }
}
```

---

## 📦 Download Sample Assets

Untuk testing cepat, download sample assets dari:

1. **Cat GIFs (transparent):**
   - https://giphy.com/search/transparent-cat
   - Filter: Stickers/GIFs with transparency

2. **Background images:**
   - https://unsplash.com (Free high-quality images)
   - Search: "gradient", "pattern", "abstract"

3. **Sounds:**
   - https://freesound.org
   - Search: "cat meow", "purr", "background music"

---

## ⚠️ Tips Penting

1. **Ukuran File:**
   - Total assets jangan lebih dari 50MB untuk menjaga ukuran APK
   - Compress images: [TinyPNG](https://tinypng.com)
   - Compress GIFs: [EZGif](https://ezgif.com/optimize)

2. **Performance:**
   - Gunakan WebP untuk images (lebih kecil dari PNG/JPG)
   - GIF sebaiknya max 3-5 seconds loop
   - Video backgrounds max 10 detik

3. **Legal:**
   - Pastikan semua assets **royalty-free** atau punya license
   - Credit creator jika diperlukan
   - Jangan gunakan copyrighted content

4. **Testing:**
   - Test di device real, bukan hanya emulator
   - Check memory usage saat loading banyak assets
   - Verify transparency pada cat objects

---

## 🐛 Troubleshooting

**Problem:** Assets tidak muncul di app
- **Solution:** Clean & Rebuild project, pastikan file ada di `app/src/main/assets/`

**Problem:** GIF tidak transparent
- **Solution:** Use [Unscreen.com](https://www.unscreen.com) atau [Remove.bg](https://remove.bg)

**Problem:** APK size terlalu besar
- **Solution:** Compress assets atau pindahkan sebagian ke remote server (download on-demand)

**Problem:** App crash saat load assets
- **Solution:** Check file corruption, reduce file size, atau tambah error handling

---

## 📞 Support

Jika ada pertanyaan atau issue dengan assets, check:
1. File permissions (`chmod` di Mac/Linux)
2. File naming (no spaces, no special characters)
3. Asset folder path (`app/src/main/assets/`)

Happy editing! 🎬✨
