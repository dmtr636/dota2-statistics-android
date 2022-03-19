package com.kodimstudio.dota2stats.model

data class HeroStat(
    val heroId: String,
    val matches: Int,
    val winrate: Float,
    val kda: Float
)
