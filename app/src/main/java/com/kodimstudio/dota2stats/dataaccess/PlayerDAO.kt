package com.kodimstudio.dota2stats.dataaccess

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.kodimstudio.dota2stats.model.Player

@Dao
interface PlayerDAO {
    @Query("SELECT * FROM player")
    fun getAll(): List<Player>

    @Query("SELECT * FROM player WHERE steamId = (:steamId)")
    fun getById(steamId: Long): List<Player>

    @Insert
    fun insertAll(vararg players: Player)

    @Delete
    fun delete(player: Player)

    @Query("DELETE FROM player WHERE steamId = (:steamId)")
    fun deleteById(steamId: Long)
}