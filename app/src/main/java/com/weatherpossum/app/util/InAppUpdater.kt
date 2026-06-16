package com.weatherpossum.app.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Interceptor
import com.weatherpossum.app.data.api.GhRelease
import com.weatherpossum.app.data.api.GitHubApi
import com.weatherpossum.app.data.api.HttpClients
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
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

        val versionName = AppVersion.normalize(rel.name ?: rel.tag_name)
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
     * Compares semantic versions (e.g., "1.5.0" vs "1.4.9") and version codes
     */
    fun isNewerThanInstalled(context: Context, tagOrSemver: String): Boolean {
        val pm = context.packageManager
        val pinfo = pm.getPackageInfo(context.packageName, 0)
        val installedVersionName = pinfo.versionName ?: "0.0.0"
        return AppVersion.isNewer(tagOrSemver, installedVersionName)
    }

    /**
     * Download a file from URL to the app's cache directory
     * @param onProgress Optional callback for download progress (0.0 to 1.0)
     */
    suspend fun downloadToCache(
        context: Context,
        url: String,
        fileName: String,
        onProgress: (suspend (Float) -> Unit)? = null
    ): File {
        return withContext(Dispatchers.IO) {
            val client = githubHttpClient()
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            require(resp.isSuccessful) { "HTTP ${resp.code}" }
            
            val body = resp.body
            val contentLength = body.contentLength()
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            val out = File(dir, fileName)

            body.source().use { src ->
                out.outputStream().sink().buffer().use { dst ->
                    val buffer = Buffer()
                    var totalBytesRead = 0L
                    
                    while (true) {
                        val bytesRead = src.read(buffer, 8192)
                        if (bytesRead == -1L) break
                        
                        dst.write(buffer, bytesRead)
                        totalBytesRead += bytesRead
                        
                        if (contentLength > 0 && onProgress != null) {
                            val progress = (totalBytesRead.toFloat() / contentLength).coerceIn(0f, 1f)
                            onProgress.invoke(progress)
                        }
                    }
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

        val current = installed.signingInfo?.apkContentsSigners
            ?.map { it.toCharsString() }
            ?.toSet()
            ?: emptySet()

        val archive = pm.getPackageArchiveInfo(
            apkFile.path,
            PackageManager.GET_SIGNING_CERTIFICATES
        ) ?: return false

        val update = archive.signingInfo?.apkContentsSigners
            ?.map { it.toCharsString() }
            ?.toSet()
            ?: emptySet()

        return current.intersect(update).isNotEmpty()
    }

    /**
     * Launch the system package installer for [apk].
     */
    fun installApk(context: Context, apk: File) {
        val activity = context.findActivity()
        val launchContext = activity ?: context.applicationContext

        if (!launchContext.packageManager.canRequestPackageInstalls()) {
            launchContext.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    "package:${launchContext.packageName}".toUri()
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }

        val uri = FileProvider.getUriForFile(
            launchContext, "${launchContext.packageName}.fileprovider", apk
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (activity != null) {
            activity.startActivity(installIntent)
        } else {
            launchContext.startActivity(installIntent)
        }
    }

    private fun Context.findActivity(): Activity? {
        var current: Context = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return current as? Activity
    }

    private fun githubHttpClient(): OkHttpClient =
        HttpClients.timedBuilder()
            .addInterceptor(githubUserAgentInterceptor())
            .build()

    private fun githubUserAgentInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "WeatherPossum-InAppUpdater")
            .build()
        chain.proceed(request)
    }

    private fun provideGitHubApi(): GitHubApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(githubHttpClient())
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()
        return retrofit.create(GitHubApi::class.java)
    }
}
