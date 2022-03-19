package com.kodimstudio.dota2stats.api

import com.kodimstudio.dota2stats.BuildConfig
import com.kodimstudio.dota2stats.api.model.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

const val API_KEY = BuildConfig.API_KEY

interface SteamApiServices {
    @GET("ISteamUser/ResolveVanityURL/v1/?key=${API_KEY}")
    fun resolveVanityUrl(@Query("vanityurl") vanityUrl: String): Call<ResolveVanityUrlResponse>

    @GET("ISteamUser/GetPlayerSummaries/v2/?key=${API_KEY}")
    fun getPlayerSummaries(@Query("steamids") steamids: Long): Call<PlayerSummariesResponse>

    @GET("api/search")
    fun searchPlayer(@Query("q") q: String): Call<List<SearchPlayerResponse>>

    @GET("api/players/{id}")
    fun getStatsSummaries(@Path("id") account_id: Int): Call<StatsSummariesResponse>

    @GET("api/players/{id}/recentMatches")
    fun getRecentMatches(@Path("id") account_id: Int): Call<List<RecentMatch>>

    @GET("api/players/{id}/matches")
    fun getMatches(@Path("id") account_id: Int): Call<List<RecentMatch>>

    @GET("api/matches/{matchId}")
    fun getMatchDetails(@Path("matchId") matchId: Long): Call<MatchDetails>

    @POST("api/request/{matchId}")
    fun makeRequest(@Path("matchId") matchId: Long): Call<MakeRequestResponse>

    @GET("api/request/{jobId}")
    fun getRequestStatus(@Path("jobId") jobId: Long): Call<RequestStatusResponse>

    @GET("IPlayerService/GetOwnedGames/v1/?key=${API_KEY}&include_played_free_games=1&appids_filter=570")
    fun getOwnedGames(@Query("steamid") steamId: Long): Call<OwnedGames>
}