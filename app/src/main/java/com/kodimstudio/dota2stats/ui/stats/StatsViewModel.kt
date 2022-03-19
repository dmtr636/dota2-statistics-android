package com.kodimstudio.dota2stats.ui.stats

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.api.RequestStatus
import com.kodimstudio.dota2stats.api.Common
import com.kodimstudio.dota2stats.api.model.OwnedGames
import com.kodimstudio.dota2stats.api.model.RecentMatch
import com.kodimstudio.dota2stats.api.model.StatsSummariesResponse
import com.kodimstudio.dota2stats.data.Data
import com.kodimstudio.dota2stats.model.*
import com.kodimstudio.dota2stats.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.random.Random

class StatsViewModel : ViewModel() {
    private val matches: MutableLiveData<List<RecentMatch>> = MutableLiveData()
    private val recentMatches: MutableLiveData<List<RecentMatch>> = MutableLiveData()
    private val generalStats: MutableLiveData<GeneralStats> = MutableLiveData()
    private val statsSummaries: MutableLiveData<StatsSummariesResponse> = MutableLiveData()
    private val heroesStats: MutableLiveData<List<HeroStat>> = MutableLiveData()
    private val itemsStats: MutableLiveData<List<ItemStat>> = MutableLiveData()
    private val matchesMap: MutableLiveData<HashMap<Long, RecentMatch>> = MutableLiveData()
    private val records: MutableLiveData<List<Record>> = MutableLiveData()
    var playtime = MutableLiveData<Int>()
    var loadHeroesStatsStatus = MutableLiveData<Int>()
    var loadItemsStatsStatus = MutableLiveData<Int>()
    var loadRecordsStatsStatus = MutableLiveData<Int>()
    var loadMatchesStatus = MutableLiveData<Int>()
    var loadGeneralStatsStatus = MutableLiveData<Int>()


    var errorStatus = MutableLiveData<Int>()

    private val serviceOpenDota = Common.retrofitServiceOpenDota
    private val serviceSteam = Common.retrofitServiceSteam
    private val dotabuffUrl = MainActivity.appContext.getString(R.string.dotabuff_url)

    lateinit var player: Player

    fun getMatches() = matches
    fun getRecentMatches() = recentMatches
    fun getGeneralStats() = generalStats
    fun getStatsSummaries() = statsSummaries
    fun getHeroesStats() = heroesStats
    fun getItemsStats() = itemsStats
    fun getMatchesMap() = matchesMap
    fun getRecords() = records

    fun loadGeneralStats(steamId: Long, accountId: Int) {
        if (loadGeneralStatsStatus.value == null) {
            loadGeneralStatsStatus.value = RequestStatus.EXECUTING
            loadPlaytime(steamId)
            loadRecentMatches(accountId)
            loadMatches(accountId)
            loadStatsSummaries(accountId)
        }
    }

    private fun loadPlaytime(steamId: Long) {
        serviceSteam.getOwnedGames(steamId).enqueue(object: Callback<OwnedGames> {
            override fun onResponse(
                call: Call<OwnedGames>,
                response: Response<OwnedGames>
            ) {
                if (!response.isSuccessful) {
                    loadGeneralStatsStatus.value = RequestStatus.ERROR
                    return
                }
                val data = response.body() as OwnedGames
                if (data.response.games == null) {
                    return
                }
                if (data.response.games.none { it.appid == 570 }) {
                    return
                }
                playtime.value = data.response.games.first { it.appid == 570 }.playtime_forever / 60
            }

            override fun onFailure(call: Call<OwnedGames>, t: Throwable) {
                loadGeneralStatsStatus.value = RequestStatus.ERROR
            }
        })
    }

    private fun loadRecentMatches(accountId: Int) {
        serviceOpenDota.getRecentMatches(accountId).enqueue(object: Callback<List<RecentMatch>> {
            override fun onResponse(
                call: Call<List<RecentMatch>>,
                response: Response<List<RecentMatch>>
            ) {
                if (!response.isSuccessful) {
                    loadGeneralStatsStatus.value = RequestStatus.ERROR
                    return
                }
                var data = response.body() as List<RecentMatch>
                if (data.isEmpty()) {
                    return
                }
                data = data.filter { recentMatch -> recentMatch.game_mode != 19 }
                recentMatches.value = data
            }

            override fun onFailure(call: Call<List<RecentMatch>>, t: Throwable) {
                loadGeneralStatsStatus.value = RequestStatus.ERROR
            }
        })
    }

    private fun loadMatches(accountId: Int) {
        serviceOpenDota.getMatches(accountId).enqueue(object: Callback<List<RecentMatch>> {
            override fun onResponse(
                call: Call<List<RecentMatch>>,
                response: Response<List<RecentMatch>>
            ) {
                if (!response.isSuccessful) {
                    loadGeneralStatsStatus.value = RequestStatus.ERROR
                    return
                }
                val data = response.body() as List<RecentMatch>
                if (data.isEmpty()) {
                    return
                }
                matches.value = data
                val tmpMap = hashMapOf<Long, RecentMatch>()
                for (match in data) {
                    tmpMap[match.match_id] = match
                }
                matchesMap.value = tmpMap
                analyzeMatches()
            }

            override fun onFailure(call: Call<List<RecentMatch>>, t: Throwable) {
                loadGeneralStatsStatus.value = RequestStatus.ERROR
            }
        })
    }

    fun analyzeMatches() {
        viewModelScope.launch(Dispatchers.Default) {
            var tmpWins = 0
            var tmpLoses = 0
            var tmpTimeSpent = 0
            var kills = 0
            var deaths = 0
            var assists = 0
            for (match in matches.value!!) {
                if (match.radiant_win && match.player_slot < 128) {
                    tmpWins++
                } else if (!match.radiant_win && match.player_slot >= 128) {
                    tmpWins++
                }
                tmpTimeSpent += match.duration
                kills += match.kills
                deaths += match.deaths
                assists += match.assists
            }
            tmpLoses = matches.value!!.size - tmpWins
            tmpTimeSpent /= 3600
            val gamesCount = tmpWins + tmpLoses
            val winrate = tmpWins / gamesCount.toFloat() * 100
            val avgKda = if (deaths == 0) {
                (kills + assists).toFloat()
            } else {
                (kills + assists) / deaths.toFloat()
            }

            generalStats.postValue(GeneralStats(
                wins = tmpWins,
                loses = tmpLoses,
                gamesCount = gamesCount,
                timeSpent = tmpTimeSpent,
                winrate = winrate,
                avgKda = avgKda
            ))
        }
    }

    private fun loadStatsSummaries(accountId: Int) {
        serviceOpenDota.getStatsSummaries(accountId).enqueue(object: Callback<StatsSummariesResponse> {
            override fun onResponse(
                call: Call<StatsSummariesResponse>,
                response: Response<StatsSummariesResponse>
            ) {
                if (!response.isSuccessful) {
                    loadGeneralStatsStatus.value = RequestStatus.ERROR
                    return
                }
                val data = response.body() as StatsSummariesResponse
                statsSummaries.value = data
            }
            override fun onFailure(call: Call<StatsSummariesResponse>, t: Throwable) {
                loadGeneralStatsStatus.value = RequestStatus.ERROR
            }
        })
    }

    fun loadHeroesStats(accountId: Int) {
        if (loadHeroesStatsStatus.value != null) {
            return
        }
        loadHeroesStatsStatus.value = RequestStatus.EXECUTING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc: Document = Jsoup.connect(dotabuffUrl +  "players/${accountId}/heroes")
                    .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")
                    .cookie("_hi", getRandomCookie())
                    .get()

                val trList = doc.select("div.content-inner table.sortable tbody tr")
                val tmpList = mutableListOf<HeroStat>()
                for (tr: Element in trList) {
                    val heroName = tr.child(0).attr("data-value")
                    val matches = tr.child(2).text().replace(",", "").toInt()
                    val winrate = tr.child(3).text().replace("%", "").toFloat()
                    val kda = tr.child(4).text().toFloat()

                    tmpList.add(HeroStat(heroName, matches, winrate, kda))
                }
                Data.HEROES_S.map { entry ->
                    if (tmpList.none { heroStat -> heroStat.heroId == entry.key }) {
                        tmpList.add(HeroStat(entry.key, 0, 0f, 0f))
                    }
                }
                heroesStats.postValue(tmpList)
                loadHeroesStatsStatus.postValue(RequestStatus.SUCCESS)
            }
            catch (e: Exception) {
                errorStatus.postValue(-1)
                e.printStackTrace()
                loadHeroesStatsStatus.postValue(RequestStatus.ERROR)
            }
        }
    }

    fun loadItemsStats(accountId: Int) {
        if (loadItemsStatsStatus.value != null) {
            return
        }
        loadItemsStatsStatus.value = RequestStatus.EXECUTING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc: Document = Jsoup.connect(dotabuffUrl + "players/${accountId}/items")
                    .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")
                    .cookie("_hi", getRandomCookie())
                    .get()
                val trList = doc.select("div.content-inner table.sortable tbody tr")
                val tmpList = mutableListOf<ItemStat>()
                for (tr: Element in trList) {
                    val itemName = tr.child(0).attr("data-value")
                    val matches = tr.child(2).text().replace(",", "").toInt()
                    val winrate = tr.child(3).text().replace("%", "").toFloat()
                    val kda = tr.child(4).text().toFloat()

                    if (Data.ITEMS_S[itemName] != null) {
                        tmpList.add(ItemStat(itemName, matches, winrate, kda))
                    }
                }
                itemsStats.postValue(tmpList)
                loadItemsStatsStatus.postValue(RequestStatus.SUCCESS)
            }
            catch (e: Exception) {
                errorStatus.postValue(-1)
                loadItemsStatsStatus.postValue(RequestStatus.ERROR)
            }
        }
    }

    fun loadRecords(accountId: Int) {
        if (loadRecordsStatsStatus.value != null) {
            return
        }
        loadRecordsStatsStatus.value = RequestStatus.EXECUTING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc: Document = Jsoup.connect( dotabuffUrl + "players/${accountId}/records")
                    .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")
                    .cookie("_hi", getRandomCookie())
                    .get()
                val blocks = doc.select(".player-records .record .card a")
                val tmpList = mutableListOf<Record>()
                for (block: Element in blocks) {
                    val title = block.child(0).text()
                    val value = block.child(1).text()
                    val matchId = block.attr("href").split("/").last().toLong()

                    tmpList.add(Record(title, value, matchId))
                }
                records.postValue(tmpList)
                loadRecordsStatsStatus.postValue(RequestStatus.SUCCESS)
            }
            catch (e: Exception) {
                errorStatus.postValue(-1)
                loadRecordsStatsStatus.postValue(RequestStatus.ERROR)
            }
        }
    }

    fun getRandomCookie(): String {
        val value = Random.nextInt(1000, 999999)
        return value.toString()
    }
}