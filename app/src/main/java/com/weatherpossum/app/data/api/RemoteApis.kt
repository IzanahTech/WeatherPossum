package com.weatherpossum.app.data.api

import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

// ── NHC hurricane feeds ──────────────────────────────────────────────────────

interface HurricaneFeedsApi {
    @GET("CurrentStorms.json")
    suspend fun currentStorms(): ResponseBody

    @GET("text/MIATWOAT.shtml")
    suspend fun atlanticTwoText(): ResponseBody
}

// ── GitHub releases (in-app updater) ─────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class GhAsset(
    val name: String,
    val browser_download_url: String,
    val size: Long
)

@JsonClass(generateAdapter = true)
data class GhRelease(
    val tag_name: String,
    val name: String?,
    val body: String?,
    val assets: List<GhAsset>
)

interface GitHubApi {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun latestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GhRelease
}
