package com.kodimstudio.dota2stats.model

data class GeneralStats (
    val wins: Int,
    val loses: Int,
    val gamesCount: Int,
    val timeSpent: Int,
    val winrate: Float,
    val avgKda: Float
)