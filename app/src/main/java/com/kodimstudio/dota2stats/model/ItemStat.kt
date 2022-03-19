package com.kodimstudio.dota2stats.model

data class ItemStat(
    val itemId: String,
    val matches: Int,
    val winrate: Float,
    val kda: Float
)
