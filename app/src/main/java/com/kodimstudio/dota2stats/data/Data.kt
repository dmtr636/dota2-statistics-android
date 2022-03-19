package com.kodimstudio.dota2stats.data

import com.kodimstudio.dota2stats.model.Hero
import com.kodimstudio.dota2stats.model.Item

class Data {
    companion object {
        val HEROES = hashMapOf<Int, Hero>()
        val HEROES_S = hashMapOf<String, Hero>()
        val ITEMS = hashMapOf<Int, Item>()
        val ITEMS_S = hashMapOf<String, Item>()
        val EXECUTING_REQUESTS = hashMapOf<Long, Long>()
    }
}


