package com.kodimstudio.dota2stats.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Player (
    @PrimaryKey
    val steamId: Long?,

    @ColumnInfo(name = "nick_name")
    val nickname: String?,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,

    val accountId: Int? = (steamId?.minus(76561197960265728L))?.toInt()
) : Serializable