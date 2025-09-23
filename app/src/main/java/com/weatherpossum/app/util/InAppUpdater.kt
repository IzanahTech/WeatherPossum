package com.weatherpossum.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import com.weatherpossum.app.data.api.GhRelease
import com.weatherpossum.app.data.api.GitHubApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.security.MessageDigest

/**
 * In-app updater using GitHub Releases
 * Handles checking for updates, downloading APKs, verifying signatures, and installing
 */
object InAppUpdater {

    data class UpdateCandidate(
        val versionName: String,
        val tag: String,
        val notes: String,
        val apkUrl: String,
        val shaUrl: String
    )

    /**
     * Check for the latest release on GitHub
     * @param context Android context
     * @param owner GitHub repository owner
     * @param repo GitHub repository name
     * @param universalNameHint Hint to prefer universal APK (default: "universal")
     * @return UpdateCandidate if a newer version is available, null otherwise
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun checkLatest(
        context: Context,
        owner: String,
        repo: String,
        universalNameHint: String = "universal"
    ): UpdateCandidate? {
        // Note: context parameter kept for future use (e.g., package info, preferences)
        val api = provideGitHubApi()
        val rel = api.latestRelease(owner, repo)

        // Find an APK asset (prefer "universal")
        val apk = rel.assets
            .filter { it.name.endsWith(".apk", ignoreCase = true) }
            .sortedByDescending { if (it.name.contains(universalNameHint, true)) 1 else 0 }
            .firstOrNull() ?: return null

        // Find its .sha256 companion (same prefix)
        val sha = rel.assets.firstOrNull {
            it.name.equals("${apk.name}.sha256", ignoreCase = true) ||
            (it.name.endsWith(".sha256", true) && it.name.startsWith(apk.name, true))
        } ?: rel.assets.firstOrNull { it.name.endsWith(".sha256", true) } // fallback: any sha256

        val versionName = rel.name ?: rel.tag_name
        return UpdateCandidate(
            versionName = versionName,
            tag = rel.tag_name,
            notes = rel.body.orEmpty(),
            apkUrl = apk.browser_download_url,
            shaUrl = sha?.browser_download_url.orEmpty()
        )
    }

    /**
     * Check if the given tag/version is newer than the currently installed version
     * For simplicity, this always returns true (treats latest as newer)
     * In production, you'd want to implement proper semantic version comparison
     */
    @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
    fun isNewerThanInstalled(context: Context, tagOrSemver: String): Boolean {
        val pm = context.packageManager
        val pinfo = pm.getPackageInfo(context.packageName, 0)
        // Note: localCode and tagOrSemver kept for future semantic version comparison
        val localCode = if (Build.VERSION.SDK_INT >= 28) 
            pinfo.longVersionCode 
        else 
            @Suppress("DEPRECATION") pinfo.versionCode.toLong()
        
        // For a quick start, assume tags monotonically increase and always treat latest as newer
        // In production, implement proper semantic version parsing and comparison
        // TODO: Use tagOrSemver and localCode for proper version comparison
        return true
    }

    /**
     * Download a file from URL to the app's cache directory
     */
    suspend fun downloadToCache(context: Context, url: String, fileName: String): File {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            require(resp.isSuccessful) { "HTTP ${resp.code}" }
            
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            val out = File(dir, fileName)
            
            resp.body!!.source().use { src ->
                out.outputStream().sink().buffer().use { dst -> 
                    dst.writeAll(src)
                }
            }
            out
        }
    }

    /**
     * Compute SHA256 hash of a file
     */
    fun computeSha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buf = ByteArray(8192)
            while (true) {
                val n = fis.read(buf)
                if (n <= 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Read SHA256 hash from a file
     */
    fun readSha256File(file: File): String =
        file.readText().trim().lowercase().split(Regex("\\s+")).first()

    /**
     * Check if the APK is signed by the same certificate as the installed app
     */
    fun isSignedBySameCert(context: Context, apkFile: File): Boolean {
        val pm = context.packageManager
        val installed = pm.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES
        )
        
        val current = if (Build.VERSION.SDK_INT >= 28)
            installed.signingInfo?.apkContentsSigners?.map { it.toCharsString() }?.toSet() ?: emptySet()
        else 
            @Suppress("DEPRECATION")
            installed.signatures?.map { it.toCharsString() }?.toSet() ?: emptySet()

        val archive = pm.getPackageArchiveInfo(
            apkFile.path,
            PackageManager.GET_SIGNING_CERTIFICATES
        ) ?: return false
        
        val update = if (Build.VERSION.SDK_INT >= 28)
            archive.signingInfo?.apkContentsSigners?.map { it.toCharsString() }?.toSet() ?: emptySet()
        else 
            @Suppress("DEPRECATION")
            archive.signatures?.map { it.toCharsString() }?.toSet() ?: emptySet()

        return current.intersect(update).isNotEmpty()
    }

    /**
     * Install an APK file using the system installer
     */
    fun installApk(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
        
        // Ensure user has allowed "Install unknown apps" for your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            context.startActivity(Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ))
            return
        }
        context.startActivity(intent)
    }

    /**
     * Create GitHub API instance with Moshi converter
     */
    private fun provideGitHubApi(): GitHubApi {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(GitHubApi::class.java)
    }
}
