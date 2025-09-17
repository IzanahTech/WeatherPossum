package com.weatherpossum.app.data.api

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * GitHub API data models for release information
 */
data class GhAsset(
    val name: String,
    val browser_download_url: String,
    val size: Long
)

data class GhRelease(
    val tag_name: String,
    val name: String?,
    val body: String?,
    val assets: List<GhAsset>
)

/**
 * GitHub API interface for fetching release information
 * Unauthenticated requests work but are rate-limited
 */
interface GitHubApi {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun latestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GhRelease
}
