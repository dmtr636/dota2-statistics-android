package com.kodimstudio.dota2stats.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kodimstudio.dota2stats.api.Common
import com.kodimstudio.dota2stats.api.model.MatchDetails
import com.kodimstudio.dota2stats.api.model.PlayerSummariesResponse
import com.kodimstudio.dota2stats.api.model.ResolveVanityUrlResponse
import com.kodimstudio.dota2stats.api.model.SearchPlayerResponse
import com.kodimstudio.dota2stats.model.Player
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchViewModel : ViewModel() {
    var playersList: MutableLiveData<MutableList<Player>> = MutableLiveData()
    var taskCounter: MutableLiveData<Int> = MutableLiveData()
    var matchDetails = MutableLiveData<MatchDetails>()
    var loadMatchDetailsStatus = MutableLiveData<Int>()
    var errorStatus = MutableLiveData<Int>()

    private val serviceOpenDota = Common.retrofitServiceOpenDota
    private val serviceSteam = Common.retrofitServiceSteam

    init {
        playersList.value = mutableListOf()
        taskCounter.value = 2
    }

    fun loadMatchDetails(matchId: Long) {
        serviceOpenDota.getMatchDetails(matchId).enqueue(object: Callback<MatchDetails> {
            override fun onResponse(call: Call<MatchDetails>, response: Response<MatchDetails>) {
                if (!response.isSuccessful) {
                    return
                }
                try {
                    val data = response.body() as MatchDetails
                    matchDetails.value = data
                } catch (e: Exception) {

                }
            }

            override fun onFailure(call: Call<MatchDetails>, t: Throwable) {

            }
        })
    }

    fun resolveVanityUrl(steamUrl: String) {
        var vanityUrl = steamUrl
        if (steamUrl.contains("steamcommunity.com/id/", true)) {
            vanityUrl = steamUrl.substringAfter("steamcommunity.com/id/").replace("/", "")
        }
        serviceSteam.resolveVanityUrl(vanityUrl).enqueue(object : Callback<ResolveVanityUrlResponse> {
            override fun onResponse(call: Call<ResolveVanityUrlResponse>, response: Response<ResolveVanityUrlResponse>) {
                if (!response.isSuccessful) {
                    errorStatus.value = -1
                    return
                }
                val data = response.body() as ResolveVanityUrlResponse
                val steamId = data.response?.steamid
                if (steamId != null) {
                    getPlayerSummaries(steamId)
                } else {
                    taskCounter.value = taskCounter.value?.minus(1)
                }
            }
            override fun onFailure(call: Call<ResolveVanityUrlResponse>, t: Throwable) {
                errorStatus.value = -1
            }
        })
    }

    fun getPlayerSummaries(steamId: Long) {
        serviceSteam.getPlayerSummaries(steamId).enqueue(object : Callback<PlayerSummariesResponse> {
            override fun onResponse(call: Call<PlayerSummariesResponse>, response: Response<PlayerSummariesResponse>) {
                if (!response.isSuccessful) {
                    errorStatus.value = -1
                    return
                }

                val data = response.body() as PlayerSummariesResponse
                if (data.response.players.isNotEmpty()) {
                    val nickname = data.response.players[0].personaname
                    val avatarUrl = data.response.players[0].avatarfull
                    val player = Player(steamId = steamId, nickname = nickname, avatarUrl = avatarUrl)
                    playersList.value = playersList.value?.apply { add(0, player) }
                }
                taskCounter.value = taskCounter.value?.minus(1)
            }

            override fun onFailure(call: Call<PlayerSummariesResponse>, t: Throwable) {
                errorStatus.value = -1
            }
        })
    }

    fun searchPlayers(personaName: String) {
        serviceOpenDota.searchPlayer(personaName).enqueue(object : Callback<List<SearchPlayerResponse>> {
            override fun onResponse(call: Call<List<SearchPlayerResponse>>, response: Response<List<SearchPlayerResponse>>) {
                if (!response.isSuccessful) {
                    errorStatus.value = -1
                    return
                }
                val data = response.body() as List<SearchPlayerResponse>
                if (data.isNotEmpty()) {
                    val tmpList = mutableListOf<Player>()
                    for (player in data) {
                        val nickname = player.personaname
                        val avatarFull = player.avatarfull
                        val accountId = player.account_id.toLong()
                        val steamId = accountId + 76561197960265728L
                        tmpList.add(Player(steamId, nickname, avatarFull))
                    }
                    playersList.value = playersList.value?.apply { addAll(tmpList) }
                    taskCounter.value = taskCounter.value?.minus(1)
                }
            }

            override fun onFailure(call: Call<List<SearchPlayerResponse>>, t: Throwable) {
                errorStatus.value = -1
            }
        })
    }
}