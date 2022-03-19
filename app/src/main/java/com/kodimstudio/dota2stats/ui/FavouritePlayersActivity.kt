package com.kodimstudio.dota2stats.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kodimstudio.dota2stats.databinding.ActivityFavouritePlayersBinding
import com.kodimstudio.dota2stats.model.Player
import com.kodimstudio.dota2stats.adapters.SearchResultListAdapter
import com.kodimstudio.dota2stats.ui.stats.StatsActivity

class FavouritePlayersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouritePlayersBinding
    lateinit var adapter: SearchResultListAdapter
    private lateinit var playersList: MutableList<Player>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavouritePlayersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        playersList = mutableListOf()

        val onPlayerClickListener = object : SearchResultListAdapter.OnPlayerClickListener{
            override fun onPlayerClick(player: Player, position: Int) {
                val intent = Intent(this@FavouritePlayersActivity, StatsActivity::class.java)
                intent.putExtra("player", player)
                startActivity(intent)
            }
        }

        adapter = SearchResultListAdapter(playersList, onPlayerClickListener)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(
            this@FavouritePlayersActivity,
            LinearLayoutManager.VERTICAL,
            false
        )

        for (player in MainActivity.db.playerDAO().getAll()) {
            playersList.add(Player(
                    steamId = player.steamId,
                    nickname = player.nickname,
                    avatarUrl = player.avatarUrl
            ))
            adapter.notifyItemInserted(playersList.size - 1)
        }
    }
}
