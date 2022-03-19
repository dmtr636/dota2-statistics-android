package com.kodimstudio.dota2stats.ui.stats

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.databinding.ActivityNavBinding
import com.kodimstudio.dota2stats.model.Player
import com.kodimstudio.dota2stats.model.HeroStat
import com.kodimstudio.dota2stats.model.ItemStat
import com.kodimstudio.dota2stats.ui.MainActivity

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavBinding
    lateinit var player: Player
    private val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = intent.getSerializableExtra("player") as Player
        viewModel.player = player

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabLayout
        tabs.setupWithViewPager(viewPager)

        val db = MainActivity.db

        if (db.playerDAO().getById(player.steamId!!).isEmpty()) {
            binding.inFavourite.setImageResource(R.drawable.star_off)
        } else {
            binding.inFavourite.setImageResource(R.drawable.star_copy)
        }

        binding.inFavourite.setOnClickListener {
            if (db.playerDAO().getById(player.steamId!!).isEmpty()) {
                db.playerDAO().insertAll(Player(
                    steamId = player.steamId!!,
                    nickname = player.nickname,
                    avatarUrl = player.avatarUrl
                ))
                binding.inFavourite.setImageResource(R.drawable.star_copy)
            } else {
                db.playerDAO().deleteById(player.steamId!!)
                binding.inFavourite.setImageResource(R.drawable.star_off)
            }
        }

        binding.nickName.text = player.nickname
    }
}