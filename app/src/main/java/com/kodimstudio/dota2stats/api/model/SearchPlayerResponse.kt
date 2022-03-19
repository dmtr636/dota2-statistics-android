package com.kodimstudio.dota2stats.api.model

data class SearchPlayerResponse(
    val account_id: Int = 0,
    val personaname: String? = null,
    val avatarfull: String? = null
)

