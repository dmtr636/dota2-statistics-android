package com.kodimstudio.dota2stats.dataaccess

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kodimstudio.dota2stats.model.Player

@Database(entities = [Player::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDAO(): PlayerDAO
}