package com.kodimstudio.dota2stats.api.model

data class RecentMatch(
    val match_id: Long,
    val player_slot: Int,
    val radiant_win: Boolean,
    val duration: Int,
    val game_mode: Int,
    val lobby_type: Int,
    val hero_id: Int,
    val start_time: Long,
    val kills: Int,
    val deaths: Int,
    val assists: Int
)
