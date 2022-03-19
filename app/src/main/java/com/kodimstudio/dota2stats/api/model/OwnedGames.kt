package com.kodimstudio.dota2stats.api.model

data class OwnedGames(
    val response: OwnedGamesResponse
) {
    data class OwnedGamesResponse(
        val games: List<OwnedGamesGame>?
    ) {
        data class OwnedGamesGame(
            val appid: Int,
            val playtime_forever: Int
        )
    }
}
